package com.example.his.api.schedule;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.his.api.db.dao.FlowRegulationDao;
import com.example.his.api.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Component
@Slf4j
public class FlowRegulationSchedule {
    @Resource
    private FlowRegulationDao flowRegulationDao;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 毎日7時から16時の間、1分ごとに診療科の推薦リストを更新します。
     */
    //@Scheduled(cron = "0,20,40 * * * * ?")
    @Scheduled(cron = "0 * 7-16 * * ?")
    @Transactional
    public void refreshFlowRegulation() {
        // Search all place
        ArrayList<HashMap> placeList = flowRegulationDao.searchAllPlace();
        placeList.forEach(one -> {
            int id = Integer.parseInt(one.get("id").toString());
            String place = one.get("place").toString();
            // Get all customer in queue
            Set<String> keys = redisTemplate.keys("flow_regulation_customer#*");
            int sum = 0;
            for (String key : keys) {
                ValueOperations ops = redisTemplate.opsForValue();
                int placeId = Integer.parseInt(ops.get(key).toString());
                if (id == placeId) {
                    sum++;
                }
            }
            HashMap param = new HashMap();
            param.put("id", id);
            param.put("realNum", sum);

            int rows = flowRegulationDao.updateRealNum(param);
            if (rows != 1) {
                throw new HisException("健康診断の待機人数の更新に失敗しました。");
            }
        });


        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation").toString();
        boolean mode = Boolean.parseBoolean(value);
        ArrayList<HashMap> list = null;
        // Auto flow regulation
        if (mode) {
            list = flowRegulationDao.searchRecommendedWithWeight();
        }
        // Manual flow regulation
        else {
            list = flowRegulationDao.searchRecommendedWithPriority();
        }
        ArrayList result = new ArrayList();
        list.forEach((one) -> {
            JSONObject json = JSONUtil.parseObj(one);
            result.add(json.toString());
        });

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.delete("flow_regulation");
                operations.opsForList().rightPushAll("flow_regulation", result);
                return null;
            }
        });
        log.debug("健康診断の受診フローのキャッシュを更新しました。");
    }
}
