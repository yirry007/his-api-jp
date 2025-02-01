package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.CustomerEntity;

import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_customer Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.CustomerEntity
*/
public interface CustomerDao {
    public Integer searchIdByTel(String tel);

    public void insert(CustomerEntity entity);

    public HashMap searchById(int id);

    public int update(Map param);
}





