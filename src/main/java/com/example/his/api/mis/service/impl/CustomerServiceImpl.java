package com.example.his.api.mis.service.impl;

import com.example.his.api.db.dao.CustomerDao;
import com.example.his.api.db.dao.OrderDao;
import com.example.his.api.mis.service.CustomerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service("MisCustomerServiceImpl")
public class CustomerServiceImpl implements CustomerService {
    @Resource
    private CustomerDao customerDao;

    @Resource
    private OrderDao orderDao;

    @Override
    public HashMap searchSummary(int id) {
        HashMap map = customerDao.searchById(id);
        map.putAll(orderDao.searchFrontStatistic(id));
        return map;
    }
}
