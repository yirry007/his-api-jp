package com.example.his.api.schedule;

import cn.hutool.core.map.MapUtil;
import com.example.his.api.async.PaymentWorkAsync;
import com.example.his.api.db.dao.OrderDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Slf4j
public class OrderSchedule {

    @Resource
    private OrderDao orderDao;

    @Resource
    private PaymentWorkAsync paymentWorkAsync;

    /**
     * 1時間ごとに実行し、30分以上支払いがない注文をキャンセルします。
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void closeUnpaymentOrder() {
        int rows = orderDao.closeOrder();
        if (rows > 0) {
            log.info(rows + "件の未払い注文をキャンセルしました。");
        }
    }

    /**
     * 1時間ごとに実行し、返金通知を受信していない注文を処理します。
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void closeTimeoutRefundOrder() {
        ArrayList<HashMap> list = orderDao.searchTimeoutRefund();
        list.forEach(map -> {
            int id = MapUtil.getInt(map, "id");
            String outRefundNo = MapUtil.getStr(map, "outRefundNo");
            paymentWorkAsync.closeTimeoutRefund(id, outRefundNo);
        });
    }
}

