package com.example.his.api.db.dao;

import cn.hutool.core.map.MapUtil;
import com.example.his.api.db.pojo.CheckupResultEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CheckupResultDao {
    @Resource
    private MongoTemplate mongoTemplate;

    public boolean insert(String uuid, List<Map> checkup) {
        CheckupResultEntity entity = new CheckupResultEntity();
        entity.setUuid(uuid);
        entity.setCheckup(checkup);
        entity.setPlace(new ArrayList<>() {{}});
        entity.setResult(new ArrayList<>() {{}});
        //Add record
        entity = mongoTemplate.insert(entity);
        return entity.get_id() != null;
    }

    public String searchIdByUuid(String uuid) {
        Criteria criteria = Criteria.where("uuid").is(uuid);
        Query query = new Query(criteria);
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);
        return entity.get_id();
    }

    public List<Map> searchCheckupByPlace(String uuid, String place) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("checkup","uuid"),
                Aggregation.unwind("$checkup"),
                Aggregation.match(Criteria.where("uuid").is(uuid).and("checkup.place").is(place))
        );

        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "checkup_result", HashMap.class);
        List<Map> list = new ArrayList<>();
        results.getMappedResults().forEach(one -> {
            HashMap map = (HashMap) one.get("checkup");
            list.add(map);
        });
        return list;
    }

    public boolean hasAlreadyCheckup(String uuid, String place) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("result","uuid"),
                Aggregation.unwind("$result"),
                Aggregation.match(Criteria.where("uuid").is(uuid).and("result.place").is(place))
        );

        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "checkup_result", HashMap.class);
        List<HashMap> list = results.getMappedResults();
        if (list.size() >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public void addResult(String uuid, String place, Map map) {
        Criteria criteria = Criteria.where("uuid").is(uuid);
        Query query = new Query(criteria);
        // Search record by UUID
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);
        List<String> placeList = entity.getPlace();
        List<Map> resultList = entity.getResult();

        // confirm result exists
        int index = 0;
        if (placeList.contains(place)) {
            for (int i = 0; i < resultList.size(); i++) {
                Map one = resultList.get(i);
                String temp = MapUtil.getStr(one, "place");
                if (place.equals(temp)) {
                    index = i;
                    break;
                }
            }
            // Overwrite exists record
            resultList.set(index, map);
        } else {
            placeList.add(place);
            resultList.add(map);
        }
        // Update record
        mongoTemplate.save(entity);
    }

    public CheckupResultEntity searchById(String id) {
        CheckupResultEntity entity = mongoTemplate.findById(id, CheckupResultEntity.class);
        return entity;
    }
}
