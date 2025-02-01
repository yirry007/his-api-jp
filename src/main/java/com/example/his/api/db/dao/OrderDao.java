package com.example.his.api.db.dao;

import com.example.his.api.db.pojo.OrderEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @author 83942
* @description table tb_order Mapper
* @createDate 2024-12-30 12:20:18
* @Entity com.example.his.api.db.pojo.OrderEntity
*/
public interface OrderDao {
    public HashMap searchFrontStatistic(int customerId);

    public boolean searchIllegalCountInDay(int customerId);

    public int closeOrder();

    public int insert(OrderEntity entity);

    public int updatePayment(Map param);

    public Integer searchCustomerId(String outTradeNo);

    public ArrayList<HashMap> searchFrontOrderByPage(Map param);

    public long searchFrontOrderCount(Map param);

    public String searchAlreadyRefund(int id);

    public HashMap searchRefundNeeded(Map param);

    public int updateOutRefundNo(Map param);

    public int updateRefundStatusByOutRefundNo(String outRefundNo);

    public ArrayList<HashMap> searchTimeoutRefund();

    public int updateRefundStatusById(int id);

    public int closeOrderById(Map param);

    public ArrayList<HashMap> searchByPage(Map param);

    public long searchCount(Map param);

    public int deleteById(int id);

    public Integer hasOwnSnapshot(Map param);

    public int updateStatus(Map param);

    public Integer hasOwnOrder(Map param);

    public HashMap searchOrderIsFinished(String uuid);
}
