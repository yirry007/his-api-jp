package com.example.his.api.front.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.his.api.db.dao.CustomerDao;
import com.example.his.api.db.dao.CustomerImDao;
import com.example.his.api.exception.HisException;
import com.example.his.api.front.service.CustomerImService;
import com.tencentyun.TLSSigAPIv2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service("FrontCustomerImServiceImpl")
@Slf4j
public class CustomerImServiceImpl implements CustomerImService {
    @Value("${tencent.im.sdkAppId}")
    private Long sdkAppId;

    @Value("${tencent.im.secretKey}")
    private String secretKey;

    @Value("${tencent.im.managerId}")
    private String managerId;

    @Value("${tencent.im.customerServiceId}")
    private String customerServiceId;

    @Resource
    private CustomerDao customerDao;

    @Resource
    private CustomerImDao customerImDao;

    private String baseUrl = "https://console.tim.qq.com/";

    @Override
    @Transactional
    public HashMap createAccount(int customerId) {
        HashMap map = customerDao.searchById(customerId);
        String tel = MapUtil.getStr(map, "tel");
        String photo = MapUtil.getStr(map, "photo");
        String account = "customer_" + customerId;
        String nickname = "顧客_" + tel;

        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        String userSig = api.genUserSig(account, 180 * 86400); // create customer sign
        // save result
        HashMap result = new HashMap();
        result.put("sdkAppId", sdkAppId);
        result.put("account", account);
        result.put("userSig", userSig);

        userSig = api.genUserSig(managerId, 180 * 86400); // create admin sign

        //confirm if created customer IM ID
        String url = baseUrl + "v4/im_open_login_svc/account_check?sdkappid=" +
                sdkAppId + "&identifier=" + managerId + "&usersig=" + userSig +
                "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";
        JSONObject json = new JSONObject();
        json.set("CheckItem", new ArrayList<>() {{
            add(new HashMap<>() {{
                put("UserID", account);
            }});
        }});
        String response = HttpUtil.post(url, json.toString());
        JSONObject entries = JSONUtil.parseObj(response);
        Integer errorCode = entries.getInt("ErrorCode");
        String errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("顧客のIMアカウント情報が見つかりませんでした：" + errorInfo);
            throw new HisException("カスタマーサポートシステムの異常です。");
        }
        JSONArray list = (JSONArray) entries.get("ResultItem");
        JSONObject object = (JSONObject) list.get(0);
        String accountStatus = object.getStr("AccountStatus");
        // confirm if exists customer IM ID
        if ("Imported".equals(accountStatus)) {
            // Save customer login time
            int rows = customerImDao.insert(customerId);
            if (rows == 0) {
                log.error("顧客のIMアカウントのログイン時間を更新できません、顧客アカウントID：" + customerId);
                throw new HisException("カスタマーサポートシステムの異常です。");
            }

            this.sendWelcomeMessage(account);
            return result;
        }

        // Create customer IM ID
        url = baseUrl + "v4/im_open_login_svc/account_import?sdkappid=" +
                sdkAppId + "&identifier=" + managerId + "&usersig=" +
                userSig + "&random=" + RandomUtil.randomInt(1, 99999999) +
                "&contenttype=json";
        json = new JSONObject();
        json.set("UserID", account);
        json.set("Nick", nickname);
        if (photo != null) {
            json.set("FaceUrl", photo);
        }
        response = HttpUtil.post(url, json.toString());
        entries = JSONUtil.parseObj(response);
        errorCode = entries.getInt("ErrorCode");
        errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("顧客IMアカウントの作成に失敗しました：" + errorInfo);
            throw new HisException("カスタマーサポートシステムの異常です。");
        }

        // Add friend
        url = baseUrl + "v4/sns/friend_add?sdkappid=" + sdkAppId +
                "&identifier=" + managerId + "&usersig=" + userSig +
                "&random=" + RandomUtil.randomInt(1, 99999999) +
                "&contenttype=json";
        json = new JSONObject();
        json.set("From_Account", account);
        json.set("AddFriendItem", new ArrayList<>() {{
            add(new HashMap() {{
                put("To_Account", customerServiceId);
                put("AddSource", "AddSource_Type_Web");
            }});
        }});
        response = HttpUtil.post(url, json.toString());
        entries = JSONUtil.parseObj(response);
        errorCode = entries.getInt("ErrorCode");
        errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("カスタマーサポートのIMフレンドの追加に失敗しました:" + errorInfo);
            throw new HisException("カスタマーサポートシステムの異常です。");
        }
        list = (JSONArray) entries.get("ResultItem");
        object = (JSONObject) list.get(0);

        int resultCode = object.getInt("ResultCode");
        String resultInfo = object.getStr("ResultInfo");
        if (resultCode != 0) {
            log.error("カスタマーサポートのIMフレンドの追加に失敗しました:" + resultInfo);
            throw new HisException("カスタマーサポートシステムの異常です。");
        }

        int rows = customerImDao.insert(customerId);
        if (rows == 0) {
            log.error("顧客IMアカウントのログイン記録を作成できませんでした，顧客アカウントID：" + customerId);
            throw new HisException("カスタマーサポートシステムの異常です。");
        }

        this.sendWelcomeMessage(account);
        return result;
    }

    private void sendWelcomeMessage(String account) {
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        // Create admin sign
        String userSig = api.genUserSig(customerServiceId, 180 * 86400);
        String url = baseUrl + "v4/openim/sendmsg?sdkappid=" + sdkAppId +
                "&identifier=" + customerServiceId + "&usersig=" + userSig +
                "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";
        JSONObject json = new JSONObject();
        json.set("SyncOtherMachine", 2);
        json.set("To_Account", account);
        json.set("MsgLifeTime", 120);
        json.set("MsgRandom", RandomUtil.randomInt(1, 99999999)); //distinct
        json.set("MsgBody", new ArrayList<>() {{
            add(new HashMap<>() {{
                put("MsgType", "TIMTextElem"); // Text message
                put("MsgContent", new HashMap<>() {{
                    put("Text", "ご親切に、こんにちは！お手伝いできることがあればお知らせください。");
                }});
            }});
        }});
        String response = HttpUtil.post(url, json.toString());
        JSONObject entries = JSONUtil.parseObj(response);
        int errorCode = entries.getInt("ErrorCode");
        String errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("歓迎メッセージの送信に失敗しました：" + errorInfo.toString());
            throw new HisException("カスタマーサポートシステムの異常です。");
        }
    }
}
