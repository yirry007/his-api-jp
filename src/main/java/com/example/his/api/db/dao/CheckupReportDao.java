package com.example.his.api.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_checkup_report Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.CheckupReportEntity
*/
public interface CheckupReportDao {
    public int insert(Map param);

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public HashMap searchById(int id);

    public int update(Map param);

    public ArrayList<Integer> searchWillGenerateReport();

    public int updateWaybill(Map param);
}




