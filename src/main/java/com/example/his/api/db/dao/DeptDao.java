package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.DeptEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_dept Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.DeptEntity
*/
public interface DeptDao {
    public ArrayList<HashMap> searchAllDept();

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public int insert(DeptEntity dept);

    public HashMap searchById(int id);

    public int update(DeptEntity dept);

    public boolean searchCanDelete(Integer[] ids);

    public int deleteByIds(Integer[] ids);
}




