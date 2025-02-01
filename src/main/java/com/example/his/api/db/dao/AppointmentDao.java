package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.AppointmentEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_appointment Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.AppointmentEntity
*/
public interface AppointmentDao {
    public ArrayList<HashMap> searchByOrderId(int orderId);

    public int insert(AppointmentEntity entity);

    public ArrayList<HashMap> searchFrontAppointmentByPage(Map param);

    public long searchFrontAppointmentCount(Map param);

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public int deleteByIds(Integer[] ids);

    public HashMap hasAppointInToday(Map param);

    public int checkin(Map param);

    public HashMap searchUuidAndSnapshotId(Map param);

    public HashMap searchSummaryById(int id);

    public int updateStatusByUuid(Map param);

    public HashMap searchByUuid(String uuid);

    public HashMap searchDataForReport(int id);

    public HashMap searchDataForWaybill(String uuid);
}




