package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.SystemEntity;

import java.util.ArrayList;
import java.util.Map;

/**
* @author 83942
* @description table tb_system Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.SystemEntity
*/
public interface SystemDao {
    public ArrayList<SystemEntity> searchAll();

    public int update(Map param);
}




