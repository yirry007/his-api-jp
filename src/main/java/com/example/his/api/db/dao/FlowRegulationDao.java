package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.FlowRegulationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_flow_regulation Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.FlowRegulationEntity
*/
public interface FlowRegulationDao {
    public ArrayList<String> searchPlaceList();

    public ArrayList<FlowRegulationEntity> searchByPage(Map param);

    public long searchCount(Map param);

    public int insert(FlowRegulationEntity entity);

    public FlowRegulationEntity searchById(int id);

    public int update(Map param);

    public int updateRealNum(Map param);

    public ArrayList<HashMap> searchRecommendedWithWeight();

    public ArrayList<HashMap> searchRecommendedWithPriority();

    public ArrayList<HashMap> searchAllPlace();

    public int deleteByIds(Integer[] ids);
}




