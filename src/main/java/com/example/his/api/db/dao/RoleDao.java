package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.RoleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 83942
 * @description table tb_role Mapper
 * @createDate 2024-12-30 12:20:18
 * @Entity com.example.his.api.db.pojo.RoleEntity
 */
public interface RoleDao {
    public ArrayList<HashMap> searchAllRole();

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public int insert(RoleEntity role);

    public HashMap searchById(int id);

    public ArrayList<Integer> searchUserIdByRoleId(int roleId);

    public int update(RoleEntity role);

    public boolean searchCanDelete(Integer[] ids);

    public int deleteByIds(Integer[] ids);
}
