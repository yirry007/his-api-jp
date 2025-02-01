package com.example.his.api.async;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.his.api.db.dao.AppointmentRestrictionDao;
import com.example.his.api.db.dao.FlowRegulationDao;
import com.example.his.api.db.dao.SystemDao;
import com.example.his.api.db.pojo.SystemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class InitializeWorkAsync {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SystemDao systemDao;

    @Resource
    private AppointmentRestrictionDao appointmentRestrictionDao;

    @Resource
    private FlowRegulationDao flowRegulationDao;

    @Async("AsyncTaskExecutor")
    public void init() {
        // Load global setting
        this.loadSystemSetting();

        // Create health checkup schedule after 2 months
        this.createAppointmentCache();

        //Cache modified schedule
        this.createFlowRegulationCache();
    }

    private void loadSystemSetting() {
        ArrayList<SystemEntity> list = systemDao.searchAll();
        list.forEach(one -> {
            redisTemplate.opsForValue().set("setting#" + one.getItem(), one.getValue());
        });
        log.debug("システム設定をキャッシュしました");
    }

    private void createAppointmentCache() {
        DateTime startDate = DateUtil.tomorrow();
        DateTime endDate = startDate.offsetNew(DateField.DAY_OF_MONTH, 60);
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);

        HashMap param = new HashMap() {{
            put("startDate", startDate.toDateStr());
            put("endDate", endDate.toDateStr());
        }};
        ArrayList<HashMap> list = appointmentRestrictionDao.searchScheduleInRange(param);
        range.forEach(one -> {
            String date = one.toDateStr();
            int maxNum = Integer.parseInt(redisTemplate.opsForValue().get("setting#appointment_number").toString());
            int realNum = 0;
            for (HashMap map : list) {
                String temp = MapUtil.getStr(map, "date");
                if (date.equals(temp)) {
                    maxNum = MapUtil.getInt(map, "num_1");
                    realNum = MapUtil.getInt(map, "num_3");
                    break;
                }
            }
            // Set cache
            HashMap cache = new HashMap();
            cache.put("maxNum", maxNum);
            cache.put("realNum", realNum);
            String key = "appointment#" + date;
            redisTemplate.opsForHash().putAll(key, cache);
            DateTime dateTime = new DateTime(date).offsetNew(DateField.DAY_OF_MONTH, 1);
            redisTemplate.expireAt(key, dateTime);

        });

        log.debug("今後60日間の健康診断人数のキャッシュが完了しました");
    }

    private void createFlowRegulationCache() {
        // Reset the queue
        flowRegulationDao.updateRealNum(new HashMap() {{
            put("realNum", 0);
        }});

        // Search the regulation type
        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation").toString();
        boolean mode = Boolean.parseBoolean(value);
        ArrayList<HashMap> list = null;
        // Ranking by auto regulation
        if (mode) {
            list = flowRegulationDao.searchRecommendedWithWeight();
        }
        // Ranking by manual regulation
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
        log.debug("健康診断の流量制御キャッシュを更新しました");
    }
}
