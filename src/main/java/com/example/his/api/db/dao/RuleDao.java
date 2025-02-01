package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.RuleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_rule Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.RuleEntity
*/
public interface RuleDao {
    public ArrayList<HashMap> searchAllRule();

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public int insert(RuleEntity entity);

    public HashMap searchById(int id);

    public int update(RuleEntity entity);

    public int deleteById(int id);
}




