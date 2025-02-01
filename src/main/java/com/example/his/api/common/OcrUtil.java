package com.example.his.api.common;

import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;
import com.example.his.api.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
@Slf4j
public class OcrUtil {
    @Value("${huawei.appKey}")
    private String appKey;

    @Value("${huawei.appSecret}")
    private String appSecret;

    public HashMap identifyWaybill(String imgBase64) throws Exception {
        Request request = new Request();
        request.setKey(appKey);
        request.setSecret(appSecret);
        request.setMethod("POST");
        request.setUrl("https://jmexpressbill.apistore.huaweicloud.com/ocr/express-bill");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // Encode base64 image string
        String encode = URLUtil.encodeAll(imgBase64);
        // Set encoded image as request body
        request.setBody("base64=" + encode);
        // Create sign
        HttpRequestBase signedRequest = Client.sign(request);
        // Creat Http Client
        CloseableHttpClient client = HttpClients.custom().build();
        // Send Http Request
        CloseableHttpResponse response = client.execute(signedRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            log.error("OCRで運送伝票の認識に失敗しました。", response.toString());
            throw new HisException("OCRで運送伝票の認識に失敗しました。");
        }
        // Get response body
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String string = EntityUtils.toString(entity, "utf-8");
            JSONObject json = JSONUtil.parseObj(string);
            int code = json.getInt("code");
            String msg = json.getStr("msg");
            if (code == 200) {
                JSONObject data = json.getJSONObject("data");
                String recName = data.getStr("recipient_name");
                String recTel = data.getStr("recipient_phone");
                String waybillCode = data.getStr("waybill_number");
                HashMap map = new HashMap() {{
                    put("recName", recName);
                    put("recTel", recTel);
                    put("waybillCode", waybillCode);
                }};
                return map;
            } else {
                log.error("OCRで運送伝票の認識に失敗しました。", msg);
                throw new HisException("OCRで運送伝票の認識に失敗しました。");
            }
        } else {
            throw new HisException("OCRのリクエストのレスポンスボディがありません。");
        }
    }
}
