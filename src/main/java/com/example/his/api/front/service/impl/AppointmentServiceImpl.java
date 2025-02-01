package com.example.his.api.front.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.db.dao.AppointmentDao;
import com.example.his.api.db.dao.AppointmentRestrictionDao;
import com.example.his.api.db.dao.OrderDao;
import com.example.his.api.db.pojo.AppointmentEntity;
import com.example.his.api.front.service.AppointmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("FrontAppointmentService")
public class AppointmentServiceImpl implements AppointmentService {
    @Resource
    private AppointmentDao appointmentDao;

    @Resource
    private AppointmentRestrictionDao appointmentRestrictionDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public String insert(AppointmentEntity entity) {
        HashMap<String, String> resultCode = new HashMap() {{
            put("full", "当日の予約は満員です。別の日を選んでください。");
            put("fail", "予約失敗しました。");
            put("success", "予約成功しました。");
        }};
        String key = "appointment#" + entity.getDate();
        String execute = (String) redisTemplate.execute(new SessionCallback() {
            @Override
            public String execute(RedisOperations operations) throws DataAccessException {
                // watching key（get the version of optimistic lock）
                operations.watch(key);
                // get cache data
                Map entry = operations.opsForHash().entries(key);
                int maxNum = Integer.parseInt(entry.get("maxNum").toString());
                int realNum = Integer.parseInt(entry.get("realNum").toString());
                if (realNum < maxNum) {
                    // start redis transaction
                    operations.multi();
                    // customer number+1
                    operations.opsForHash().increment(key, "realNum", 1);
                    // commit transaction
                    List<Long> list = operations.exec();
                    if (list.size() == 0) {
                        return resultCode.get("fail");
                    }
                    long num = list.get(0);
                    return resultCode.get(num > 0 ? "success" : "fail");
                } else {
                    operations.unwatch();
                    return resultCode.get("full");
                }
            }
        });

        // redis transaction commit failed
        if (!execute.equals(resultCode.get("success"))) {
            return execute;
        }

        int rows = appointmentDao.insert(entity);
        if (rows != 1) {
            return resultCode.get("fail");
        }

        Map entry = redisTemplate.opsForHash().entries(key);
        int maxNum = Integer.parseInt(entry.get("maxNum").toString());

        HashMap param = new HashMap() {{
            put("date", entity.getDate());
            put("num_1", maxNum);
            put("num_2", maxNum);
            put("num_3", 1);
        }};

        // Update customer real number
        rows = appointmentRestrictionDao.saveOrUpdateRealNum(param);
        if (rows == 0) {
            return resultCode.get("fail");
        }

        // Update order status
        int orderId = entity.getOrderId();
        param = new HashMap() {{
            put("status", 5);
            put("id", orderId);
        }};
        rows = orderDao.updateStatus(param);
        if (rows == 0) {
            return resultCode.get("fail");
        }

        return resultCode.get("success");
    }

    @Override
    public PageUtils searchByPage(Map param) {
        ArrayList<HashMap> list = new ArrayList<>();
        long count = appointmentDao.searchFrontAppointmentCount(param);
        if (count > 0) {
            list = appointmentDao.searchFrontAppointmentByPage(param);
        }
        int page = MapUtil.getInt(param, "page");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, page, length);
        return pageUtils;
    }
}
