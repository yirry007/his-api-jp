package com.example.his.api.mis.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.his.api.common.FaceAuthUtil;
import com.example.his.api.common.MinioUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.db.dao.*;
import com.example.his.api.exception.HisException;
import com.example.his.api.mis.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service("MisAppointmentServiceImpl")
public class AppointmentServiceImpl implements AppointmentService {
    @Resource
    private AppointmentDao appointmentDao;

    @Resource
    private GoodsSnapshotDao goodsSnapshotDao;

    @Resource
    private CheckupResultDao checkupResultDao;

    @Resource
    private FaceAuthUtil faceAuthUtil;

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private OrderDao orderDao;

    @Resource
    private CheckupReportDao checkupReportDao;

    @Override
    public ArrayList<HashMap> searchByOrderId(int orderId) {
        ArrayList<HashMap> list = appointmentDao.searchByOrderId(orderId);
        return list;
    }

    @Override
    public PageUtils searchByPage(Map param) {
        ArrayList<HashMap> list = new ArrayList<>();
        long count = appointmentDao.searchCount(param);
        if (count > 0) {
            list = appointmentDao.searchByPage(param);
        }
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public int deleteByIds(Integer[] ids) {
        int rows = appointmentDao.deleteByIds(ids);
        return rows;
    }

    @Override
    public int hasAppointInToday(Map param) {
        HashMap map = appointmentDao.hasAppointInToday(param);
        if (map == null) {
            return 0; //no appoint
        } else if (MapUtil.getInt(map, "status") != 1) {
            return -1; //has appointed
        } else {
            return 1; //appoint
        }
    }

    @Override
    @Transactional
    public boolean checkin(Map param) {
        String pid = MapUtil.getStr(param, "pid");
        String name = MapUtil.getStr(param, "name");
        String sex = IdcardUtil.getGenderByIdCard(pid) == 1 ? "男" : "女";
        String photo_1 = MapUtil.getStr(param, "photo_1");
        String photo_2 = MapUtil.getStr(param, "photo_2");

        //verify face
        boolean result = faceAuthUtil.verifyFaceModel(name, pid, sex, photo_1, photo_2);
        if (result) {
            //save user image to minio
            String filename = pid + "_" + new DateTime().toDateStr() + ".jpg";
            String path = "checkin/" + filename;
            minioUtil.uploadImage(path, photo_2);
            //update appointment
            int rows = appointmentDao.checkin(param);
            if (rows != 1) {
                throw new HisException("予約記録の保存に失敗しました。");
            }

            HashMap map = appointmentDao.searchUuidAndSnapshotId(param);
            String uuid = MapUtil.getStr(map, "uuid");
            String snapshotId = MapUtil.getStr(map, "snapshotId");
            List<Map> checkup = goodsSnapshotDao.searchCheckup(snapshotId, sex);

            boolean bool = checkupResultDao.insert(uuid, checkup);
            if (!bool) {
                throw new HisException("健康診断結果の追加に失敗しました。");
            }
        }
        return result;
    }

    @Override
    public HashMap searchGuidanceInfo(int id) {
        HashMap map = appointmentDao.searchSummaryById(id);
        String snapshotId = MapUtil.getStr(map, "snapshotId");
        String sex = MapUtil.getStr(map, "sex");
        //create base64 qrcode image
        String uuid = MapUtil.getStr(map, "uuid");
        QrConfig qrConfig = new QrConfig();
        qrConfig.setWidth(100);
        qrConfig.setHeight(100);
        qrConfig.setMargin(0);
        String qrCodeBase64 = QrCodeUtil.generateAsBase64(uuid, qrConfig, "jpg");
        map.put("qrCodeBase64", qrCodeBase64);

        List<Map> list = goodsSnapshotDao.searchCheckup(snapshotId, sex);
        LinkedHashSet<Map> set = new LinkedHashSet();
        list.forEach(one -> {
            HashMap temp = new HashMap() {{
                put("place", MapUtil.getStr(one, "place"));
                put("name", MapUtil.getStr(one, "name"));
            }};
            set.add(temp);
        });
        map.put("checkup", set);
        return map;
    }

    @Override
    @Transactional
    public boolean updateStatusByUuid(Map param) {
        int rows = appointmentDao.updateStatusByUuid(param);
        if (rows != 1) {
            return false;
        }
        int status = MapUtil.getInt(param, "status");
        if (status == 3) {
            //check order is finished
            String uuid = MapUtil.getStr(param, "uuid");
            HashMap map = orderDao.searchOrderIsFinished(uuid);
            int orderId = MapUtil.getInt(map, "id");
            int n1 = MapUtil.getInt(map, "n1");
            int n2 = MapUtil.getInt(map, "n2");
            if (n1 == n2) {
                //update order finish
                rows = orderDao.updateStatus(new HashMap<>() {{
                    put("status", 6);
                    put("id", orderId);
                }});
                if (rows != 1) {
                    return false;
                }
            }

            String resultId = checkupResultDao.searchIdByUuid(uuid);
            param.put("resultId", resultId);
            rows = checkupReportDao.insert(param);
            if (rows != 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public HashMap searchByUuid(String uuid) {
        HashMap map = appointmentDao.searchByUuid(uuid);
        if (map == null) {
            throw new HisException("存在しない健康診断の予約記録です。");
        }
        Integer status = MapUtil.getInt(map, "status");
        if (status == 1) {
            throw new HisException("この健康診断には予約がありません。");
        } else if (status == 3) {
            throw new HisException("この健康診断の予約は終了しました。");
        }
        return map;
    }
}
