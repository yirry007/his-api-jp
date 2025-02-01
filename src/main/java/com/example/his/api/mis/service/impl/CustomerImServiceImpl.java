package com.example.his.api.mis.service.impl;

import com.example.his.api.mis.service.CustomerImService;
import com.tencentyun.TLSSigAPIv2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service("MisCustomerImServiceImpl")
public class CustomerImServiceImpl implements CustomerImService {
    @Value("${tencent.im.sdkAppId}")
    private Long sdkAppId;

    @Value("${tencent.im.secretKey}")
    private String secretKey;

    @Value("${tencent.im.customerServiceId}")
    private String customerServiceId;

    @Override
    public HashMap searchServiceAccount() {
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        //Create customer account sign
        String userSig = api.genUserSig(customerServiceId, 180 * 86400);

        HashMap result = new HashMap();
        result.put("sdkAppId", sdkAppId);
        result.put("account", customerServiceId);
        result.put("userSig", userSig);
        return result;
    }
}
