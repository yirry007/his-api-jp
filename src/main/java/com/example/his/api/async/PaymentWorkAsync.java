package com.example.his.api.async;

import com.example.his.api.db.dao.OrderDao;
import com.example.his.api.exception.HisException;
import com.example.his.api.front.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class PaymentWorkAsync {

    @Resource
    private OrderDao orderDao;

    @Resource
    private PaymentService paymentService;

    @Async("AsyncTaskExecutor") //Execute by free thread in thread pool
    @Transactional
    public void closeTimeoutRefund(int id, String outRefundNo) {
        // Search refund result
        String result = paymentService.searchRefundResult(outRefundNo);
        if ("SUCCESS".equals(result)) {
            // Update order status to refunded
            int rows = orderDao.updateRefundStatusById(id);
            if (rows != 1) {
                throw new HisException("注文ステータスの更新が失敗しました。");
            }
        } else if ("ABNORMAL".equals(result)) {
            /*
             * 1.Confirm send SMS to user
             * 2.If not, Send SMS to user
             */
        }
    }
}
