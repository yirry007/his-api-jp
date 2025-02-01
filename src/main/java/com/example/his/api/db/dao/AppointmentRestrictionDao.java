package com.example.his.api.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_appointment_restriction Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.AppointmentRestrictionEntity
*/
public interface AppointmentRestrictionDao {
    public ArrayList<HashMap> searchScheduleInRange(Map param);

    public int saveOrUpdateRealNum(Map param);
}




