package com.example.his.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.felord.payment.wechat.v3.WechatApiProvider;
import cn.felord.payment.wechat.v3.model.ResponseSignVerifyParams;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.example.his.api.common.PageUtils;
import com.example.his.api.common.R;
import com.example.his.api.config.sa_token.StpCustomerUtil;
import com.example.his.api.front.controller.form.*;
import com.example.his.api.front.service.OrderService;
import com.example.his.api.socket.WebSocketService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("FrontOrderController")
@RequestMapping("/front/order")
@Slf4j
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private WechatApiProvider wechatApiProvider;

    @PostMapping("/createPayment")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R createPayment(@RequestBody @Valid CreatePaymentForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map param = BeanUtil.beanToMap(form);
        param.put("customerId", customerId);
        HashMap map = orderService.createPayment(param);
        if (map == null) {
            return R.ok().put("illegal", true);
        } else {
            return R.ok().put("illegal", false).put("result", map);
        }
    }

    @SneakyThrows
    @PostMapping("/paymentCallback")
    public Map paymentCallback(
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            HttpServletRequest request) {
        String body = request.getReader().lines().collect(Collectors.joining());
        // Verify signature
        ResponseSignVerifyParams params = new ResponseSignVerifyParams();
        params.setWechatpaySerial(serial);
        params.setWechatpaySignature(signature);
        params.setWechatpayTimestamp(timestamp);
        params.setWechatpayNonce(nonce);
        params.setBody(body);
        return wechatApiProvider.callback("his-vue").transactionCallback(params, data -> {
            String transactionId = data.getTransactionId();
            String outTradeNo = data.getOutTradeNo();
            //Update order status and payment ID
            boolean bool = orderService.updatePayment(new HashMap() {{
                put("outTradeNo", outTradeNo);
                put("transactionId", transactionId);
            }});
            // Send payment info by websocket
            if (bool) {
                log.debug("注文の支払いが完了し、状態が更新されました。");
                // search order customerId
                Integer customerId = orderService.searchCustomerId(outTradeNo);
                if (customerId == null) {
                    log.error("customerIdが見つかりませんでした。");
                } else {
                    JSONObject json = new JSONObject();
                    json.set("result", true);
                    WebSocketService.sendInfo(json.toString(), "customer_" + customerId.toString());
                }
            } else {
                log.error("注文の支払いは完了しましたが、ステータスの更新に失敗しました。");
            }
        });
    }

    @PostMapping("/searchPaymentResult")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R searchPaymentResult(@Valid @RequestBody SearchPaymentResultForm form) {
        boolean bool = orderService.searchPaymentResult(form.getOutTradeNo());
        return R.ok().put("result", bool);
    }

    @PostMapping("/searchByPage")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R searchByPage(@RequestBody @Valid SearchOrderByPageForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        Map param = BeanUtil.beanToMap(form);
        param.put("start", start);
        param.put("customerId", customerId);
        PageUtils pageUtils = orderService.searchByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/refund")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R refund(@RequestBody @Valid RefundForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        form.setCustomerId(customerId);
        Map param = BeanUtil.beanToMap(form);
        boolean bool = orderService.refund(param);
        return R.ok().put("result", bool);
    }

    @SneakyThrows
    @PostMapping("/refundCallback")
    public Map refundCallback(
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            HttpServletRequest request) {
        String body = request.getReader().lines().collect(Collectors.joining());
        // Verify signature
        ResponseSignVerifyParams params = new ResponseSignVerifyParams();
        params.setWechatpaySerial(serial);
        params.setWechatpaySignature(signature);
        params.setWechatpayTimestamp(timestamp);
        params.setWechatpayNonce(nonce);
        params.setBody(body);
        return wechatApiProvider.callback("his-vue").refundCallback(params, data -> {
            // confirm refund status
            String status = data.getRefundStatus().toString();
            if ("SUCCESS".equals(status)) {
                String outRefundNo = data.getOutRefundNo();
                // Update order status to refund success
                boolean bool = orderService.updateRefundStatus(outRefundNo);
                if (!bool) {
                    log.error("注文状態の更新に失敗しました。");
                } else {
                    log.debug("返金流水番号" + outRefundNo + "の注文は返金が成功しました。");
                }
            } else if ("ABNORMAL".equals(status)) {
                // Refund by another method
            }
        });
    }

    @PostMapping("/payOrder")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R payOrder(@RequestBody @Valid PayOrderForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        String qrCodeBase64 = orderService.payOrder(customerId, form.getOutTradeNo());
        return R.ok().put("result", qrCodeBase64 != null).put("qrCodeBase64", qrCodeBase64);
    }

    @PostMapping("/closeOrderById")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R closeOrderById(@RequestBody @Valid CloseOrderByIdForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        form.setCustomerId(customerId);
        Map param = BeanUtil.beanToMap(form);
        boolean bool = orderService.closeOrderById(param);
        return R.ok().put("result", bool);
    }
}
