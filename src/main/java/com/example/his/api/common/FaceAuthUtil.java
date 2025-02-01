package com.example.his.api.common;

import com.example.his.api.exception.HisException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CompareFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.CompareFaceResponse;
import com.tencentcloudapi.iai.v20200303.models.DetectLiveFaceRequest;
import com.tencentcloudapi.iai.v20200303.models.DetectLiveFaceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;
import com.tencentcloudapi.iai.v20200303.models.*;

@Component
@Slf4j
public class FaceAuthUtil {
    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupId}")
    private String groupId;

    @Value("${tencent.cloud.face.region}")
    private String region;

    /**
     * Face recognition + Vivo validation
     * @param name    name
     * @param pid     ID No.
     * @param sex     gender
     * @param photo_1 Card image
     * @param photo_2 Camera image
     * @return
     */
    public boolean verifyFaceModel(String name, String pid, String sex, String photo_1, String photo_2) {
        boolean result;

        Credential cred = new Credential(secretId, secretKey);
        IaiClient client = new IaiClient(cred, region);

        //Execute face recognition
        CompareFaceRequest compareFaceRequest = new CompareFaceRequest();
        compareFaceRequest.setImageA(photo_1);
        compareFaceRequest.setImageB(photo_2);
        CompareFaceResponse compareFaceResponse = null;
        try {
            compareFaceResponse = client.CompareFace(compareFaceRequest);
        } catch (TencentCloudSDKException e) {
            log.error("顔認証の照合に失敗しました。", e);
            throw new HisException("顔認証の照合に失敗しました。");
        }
        //Get face recognition score
        Float score = compareFaceResponse.getScore();
        if (score >= 50) {
            //Detect live face request
            DetectLiveFaceRequest detectLiveFaceRequest = new DetectLiveFaceRequest();
            detectLiveFaceRequest.setImage(photo_2);
            DetectLiveFaceResponse detectLiveFaceResponse = null;
            try {
                detectLiveFaceResponse = client.DetectLiveFace(detectLiveFaceRequest);
            } catch (TencentCloudSDKException e) {
                log.error("静的な生体認証に失敗しました。", e);
                throw new HisException("静的な生体認証に失敗しました。");
            }
            result = detectLiveFaceResponse.getIsLiveness();
        } else {
            result = false;
            return result;
        }

        if (result) {
            // Get data in person base
            GetPersonBaseInfoRequest getPersonBaseInfoRequest = new GetPersonBaseInfoRequest();
            getPersonBaseInfoRequest.setPersonId(pid);
            GetPersonBaseInfoResponse getPersonBaseInfoResponse = null;
            try {
                getPersonBaseInfoResponse = client.GetPersonBaseInfo(getPersonBaseInfoRequest);
            } catch (TencentCloudSDKException e) {
                if (!e.getErrorCode().equals("InvalidParameterValue.PersonIdNotExist")) {
                    log.error("人物データベースの検索に失敗しました。", e);
                    throw new HisException("人物データベースの検索に失敗しました。");
                }
            }

            if (getPersonBaseInfoResponse == null) {
                CreatePersonRequest createPersonRequest = new CreatePersonRequest();
                createPersonRequest.setGroupId(groupId);
                createPersonRequest.setPersonId(pid);
                long gender = sex.equals("男") ? 1L : 2L;
                createPersonRequest.setGender(gender);
                createPersonRequest.setQualityControl(4L);
                createPersonRequest.setUniquePersonControl(4L);
                createPersonRequest.setPersonName(name);
                createPersonRequest.setImage(photo_1);
                CreatePersonResponse createPersonResponse = null;
                try {
                    createPersonResponse = client.CreatePerson(createPersonRequest);
                } catch (TencentCloudSDKException e) {
                    log.error("人物データベースに追加するのに失敗しました。",e);
                    throw new HisException("人物データベースに追加するのに失敗しました。");
                }
                if (StrUtil.isNotBlank(createPersonResponse.getFaceId())) {
                    log.debug("人物データベースに追加するのに成功しました。");
                } else {
                    log.error("人物データベースに追加するのに失敗しました。");
                    throw new HisException("人物データベースに追加するのに失敗しました。");
                }
            }
        }

        return result;
    }
}
