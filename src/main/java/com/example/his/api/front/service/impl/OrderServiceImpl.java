package com.example.his.api.front.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.db.dao.GoodsDao;
import com.example.his.api.db.dao.GoodsSnapshotDao;
import com.example.his.api.db.dao.OrderDao;
import com.example.his.api.db.pojo.GoodsSnapshotEntity;
import com.example.his.api.db.pojo.OrderEntity;
import com.example.his.api.exception.HisException;
import com.example.his.api.front.service.OrderService;
import com.example.his.api.front.service.PaymentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("FrontOrderServiceImpl")
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private PaymentService paymentService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private GoodsSnapshotDao goodsSnapshotDao;

    private String paymentNotifyUrl = "/front/order/paymentCallback";

    private String refundNotifyUrl = "/front/order/refundCallback";

    @Override
    @Transactional
    public HashMap createPayment(Map param) {
        int goodsId = MapUtil.getInt(param, "goodsId");
        Integer number = MapUtil.getInt(param, "number");
        int customerId = MapUtil.getInt(param, "customerId");

        //当日にその顧客に未払いの注文が10件以上、または返金の注文が5件以上ある場合、注文ができません。
        boolean illegal = orderDao.searchIllegalCountInDay(customerId);
        if (illegal) {
            return null;
        }
        // Search goods info
        HashMap map = goodsDao.searchSnapshotNeededById(goodsId);
        String goodsCode = MapUtil.getStr(map, "code");
        String goodsTitle = MapUtil.getStr(map, "title");
        String goodsDescription = MapUtil.getStr(map, "description");
        String goodsImage = MapUtil.getStr(map, "image");
        BigDecimal goodsInitialPrice = new BigDecimal(MapUtil.getStr(map, "initialPrice"));
        BigDecimal goodsCurrentPrice = new BigDecimal(MapUtil.getStr(map, "currentPrice"));
        String goodsRuleName = MapUtil.getStr(map, "ruleName");
        String goodsRule = MapUtil.getStr(map, "rule");
        String goodsType = MapUtil.getStr(map, "type");
        String goodsMd5 = MapUtil.getStr(map, "md5");

        String temp = MapUtil.getStr(map, "checkup_1");
        List<Map> goodsCheckup_1 = temp != null ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "checkup_2");
        List<Map> goodsCheckup_2 = temp != null ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "checkup_3");
        List<Map> goodsCheckup_3 = temp != null ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "checkup_4");
        List<Map> goodsCheckup_4 = temp != null ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "checkup");
        List<Map> goodsCheckup = temp != null ? JSONUtil.parseArray(temp).toList(Map.class) : null;

        temp = MapUtil.getStr(map, "tag");
        List<String> goodsTag = temp != null ? JSONUtil.parseArray(temp).toList(String.class) : null;

        ExpressRunner runner = new ExpressRunner();
        DefaultContext<String, Object> context = new DefaultContext<String, Object>();
        context.put("number", number.intValue());
        context.put("price", goodsCurrentPrice.toString());

        String amount = null;
        if (goodsRule != null) {
            try {
                // calculate price by express runner
                amount = runner.execute(goodsRule, context, null, true, false).toString();
            } catch (Exception e) {
                throw new HisException("ルールエンジンによる価格計算に失敗しました。", e);
            }
        } else {
            amount = goodsCurrentPrice.multiply(new BigDecimal(number)).toString();
        }

        //change money unit
        int total = NumberUtil.mul(amount, "100").intValue();
        //Create trade number
        String outTradeNo = IdUtil.simpleUUID().toUpperCase();

        //set expire time (20 minutes)
        DateTime dateTime = new DateTime();
        dateTime.offset(DateField.MINUTE, 20);
        String timeExpire = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        //create wechat unified order
        ObjectNode objectNode = paymentService.unifiedOrder(outTradeNo, total, "健康診断セットを購入する。", paymentNotifyUrl, timeExpire);
        String codeUrl = objectNode.get("code_url").textValue();

        // set the payment code url
        String key = "codeUrl_" + customerId + "_" + outTradeNo;
        redisTemplate.opsForValue().set(key, codeUrl);
        redisTemplate.expireAt(key, dateTime); //set expire time

        if (codeUrl != null) {
            //check snapshot exists
            String _id = goodsSnapshotDao.hasGoodsSnapshot(goodsMd5);
            //create snapshot
            if (_id == null) {
                GoodsSnapshotEntity entity = new GoodsSnapshotEntity();
                entity.setId(goodsId);
                entity.setCode(goodsCode);
                entity.setTitle(goodsTitle);
                entity.setDescription(goodsDescription);
                entity.setCheckup_1(goodsCheckup_1);
                entity.setCheckup_2(goodsCheckup_2);
                entity.setCheckup_3(goodsCheckup_3);
                entity.setCheckup_4(goodsCheckup_4);
                entity.setCheckup(goodsCheckup);
                entity.setImage(goodsImage);
                entity.setInitialPrice(goodsInitialPrice);
                entity.setCurrentPrice(goodsCurrentPrice);
                entity.setType(goodsType);
                entity.setTag(goodsTag);
                entity.setRuleName(goodsRuleName);
                entity.setRule(goodsRule);
                entity.setMd5(goodsMd5);

                // save snapshot and get its id
                _id = goodsSnapshotDao.insert(entity);
            }

            OrderEntity entity = new OrderEntity();
            entity.setCustomerId(customerId);
            entity.setGoodsId(goodsId);
            entity.setSnapshotId(_id); //set snapshot id

            entity.setGoodsTitle(goodsTitle);
            entity.setGoodsPrice(goodsCurrentPrice);
            entity.setNumber(number);
            entity.setAmount(new BigDecimal(amount));
            entity.setGoodsImage(goodsImage);
            entity.setGoodsDescription(goodsDescription);
            entity.setOutTradeNo(outTradeNo);
            // save order
            orderDao.insert(entity);

            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(230);
            qrConfig.setHeight(230);
            qrConfig.setMargin(2);
            String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");

            // Update sales volume
            int rows = goodsDao.updateSalesVolume(goodsId);
            if (rows != 1) {
                throw new HisException("商品売上の更新に失敗しました。");
            }
            return new HashMap() {{
                put("qrCodeBase64", qrCodeBase64);
                put("outTradeNo", outTradeNo);
            }};
        } else {
            log.error("支払い注文の作成に失敗しました。", objectNode);
            throw new HisException("支払い注文の作成に失敗しました。");
        }
    }

    @Override
    @Transactional
    public boolean updatePayment(Map param) {
        int rows = orderDao.updatePayment(param);
        return rows == 1;
    }

    @Override
    public Integer searchCustomerId(String outTradeNo) {
        Integer customerId = orderDao.searchCustomerId(outTradeNo);
        return customerId;
    }

    @Override
    @Transactional
    public boolean searchPaymentResult(String outTradeNo) {
        String transactionId = paymentService.searchPaymentResult(outTradeNo);
        if (transactionId != null) {
            this.updatePayment(new HashMap() {{
                put("outTradeNo", outTradeNo);
                put("transactionId", transactionId);
            }});
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PageUtils searchByPage(Map param) {
        ArrayList<HashMap> list = new ArrayList<>();
        long count = orderDao.searchFrontOrderCount(param);
        if (count > 0) {
            list = orderDao.searchFrontOrderByPage(param);
        }
        int page= (Integer) param.get("page");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, page, length);
        return pageUtils;
    }

    @Override
    @Transactional
    public boolean refund(Map param) {
        //get refund number
        int id = MapUtil.getInt(param, "id");
        String outRefundNo = orderDao.searchAlreadyRefund(id);
        //confirm has refunded
        if (outRefundNo != null) {
            return false;
        }

        HashMap map = orderDao.searchRefundNeeded(param);
        String transactionId = MapUtil.getStr(map, "transactionId");
        String amount = MapUtil.getStr(map, "amount");

        //int total = NumberUtil.mul(amount, "100").intValue();  //Total price
        int total = 1;
        int refund = total;   //refund price

        if (transactionId == null) {
            log.error("transactionIdはありません。");
            return false;
        }
        //Refund
        outRefundNo = paymentService.refund(transactionId, refund, total, refundNotifyUrl);
        param.put("outRefundNo", outRefundNo);
        if (outRefundNo != null) {
            // Update refund number and date
            int rows = orderDao.updateOutRefundNo(param);
            if (rows == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateRefundStatus(String outRefundNo) {
        int rows = orderDao.updateRefundStatusByOutRefundNo(outRefundNo);
        return rows == 1;
    }

    @Override
    public String payOrder(int customerId, String outTradeNo) {
        String key = "codeUrl_" + customerId + "_" + outTradeNo;
        if (redisTemplate.hasKey(key)) {
            // Get payment url from redis
            String codeUrl = redisTemplate.opsForValue().get(key).toString();
            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(230);
            qrConfig.setHeight(230);
            qrConfig.setMargin(2);
            String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");
            return qrCodeBase64;
        }
        return null;
    }

    @Override
    public boolean closeOrderById(Map param) {
        int rows = orderDao.closeOrderById(param);
        return rows == 1;
    }

    @Override
    public boolean hasOwnOrder(Map param) {
        Integer id = orderDao.hasOwnOrder(param);
        return id != null;
    }
}
