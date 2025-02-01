package com.example.his.api.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.his.api.common.MinioUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.db.dao.GoodsDao;
import com.example.his.api.db.pojo.GoodsEntity;
import com.example.his.api.exception.HisException;
import com.example.his.api.mis.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("MisGoodsServiceImpl")
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private MinioUtil minioUtil;

    @Override
    public PageUtils searchByPage(Map param) {
        ArrayList<HashMap> list = new ArrayList<>();
        long count = goodsDao.searchCount(param);
        if (count > 0) {
            list = goodsDao.searchByPage(param);
        }
        int page = MapUtil.getInt(param, "page");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, page, length);
        return pageUtils;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        // Create new file name
        String filename = IdUtil.simpleUUID() + ".jpg";
        String path = "front/goods/" + filename;
        minioUtil.uploadImage(path, file);
        return path;
    }

    @Override
    @Transactional
    public int insert(GoodsEntity entity) {
        // Calculate goods MD5 value
        String md5 = this.genEntityMd5(entity);
        entity.setMd5(md5);
        // save goods record
        int rows = goodsDao.insert(entity);
        return rows;
    }

    @Override
    public HashMap searchById(int id) {
        Map param = new HashMap() {{
            put("id", id);
            put("status", true);
        }};
        HashMap map = goodsDao.searchById(param);
        if (map != null) {
            for (String key : new String[]{"tag", "checkup_1", "checkup_2", "checkup_3", "checkup_4"}) {
                String temp = MapUtil.getStr(map, key);
                JSONArray array = JSONUtil.parseArray(temp);
                map.replace(key, array);
            }
            return map;
        }
        return null;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#entity.id")
    public int update(GoodsEntity entity) {
        // reset goods MD5 value
        String md5 = this.genEntityMd5(entity);
        entity.setMd5(md5);
        int rows = goodsDao.update(entity);
        return rows;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#id")
    public void updateCheckup(int id, MultipartFile file) {
        // save excel data in ArrayList
        ArrayList list = new ArrayList();

        //read excel file
        try (InputStream in = file.getInputStream();
             BufferedInputStream bin = new BufferedInputStream(in);
        ) {
            XSSFWorkbook workbook = new XSSFWorkbook(bin);
            //read sheet 1
            XSSFSheet sheet = workbook.getSheetAt(0);
            //read from second line
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);

                XSSFCell cell_1 = row.getCell(0);
                String value_1 = cell_1.getStringCellValue();

                XSSFCell cell_2 = row.getCell(1);
                String value_2 = cell_2.getStringCellValue();

                XSSFCell cell_3 = row.getCell(2);
                String value_3 = cell_3.getStringCellValue();

                XSSFCell cell_4 = row.getCell(3);
                String value_4 = cell_4.getStringCellValue();

                XSSFCell cell_5 = row.getCell(4);
                String value_5 = cell_5.getStringCellValue();

                XSSFCell cell_6 = row.getCell(5);
                String value_6 = cell_6.getStringCellValue();

                XSSFCell cell_7 = row.getCell(6);
                String value_7 = cell_7.getStringCellValue();

                XSSFCell cell_8 = row.getCell(7);
                String value_8 = cell_8.getStringCellValue();

                LinkedHashMap map = new LinkedHashMap() {{
                    put("place", value_1);
                    put("name", value_2);
                    put("item", value_3);
                    put("type", value_4);
                    put("code", value_5);
                    put("sex", value_6);
                    put("value", value_7);
                    put("template", value_8);
                }};

                list.add(map);
            }
            //test print
            System.out.println(list);
        } catch (Exception e) {
            throw new HisException("Excelファイルの処理に失敗しました。", e);
        }

        if (list.size() == 0) {
            throw new HisException("文書の内容が無効です。");
        }

        // save file in minio
        String path = "/mis/goods/checkup/" + id + ".xlsx";
        minioUtil.uploadExcel(path, file);

        // update checkup and md5 value
        GoodsEntity entity = goodsDao.searchEntityById(id);
        String temp = JSONUtil.parseArray(list).toString();
        entity.setCheckup(temp);
        String md5 = this.genEntityMd5(entity);
        HashMap param = new HashMap() {{
            put("id", id);
            put("checkup", temp);
            put("md5", md5);
        }};
        //save date
        int rows = goodsDao.updateCheckup(param);
        if (rows != 1) {
            throw new HisException("健康診断内容の更新に失敗しました。");
        }

    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#param.get('id')",
            condition = "#param.get('status')==false")
    public boolean updateStatus(Map param) {
        int rows = goodsDao.updateStatus(param);
        boolean bool = (rows == 1);
        return bool;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#ids")
    public int deleteByIds(Integer[] ids) {
        //get goods image address
        ArrayList<String> list = goodsDao.searchImageByIds(ids);
        //delete record
        int rows = goodsDao.deleteByIds(ids);
        if (rows > 0) {
            //delete goods image
            list.forEach((path) -> {
                minioUtil.deleteFile(path);
            });
        }
        return rows;
    }

    private String genEntityMd5(GoodsEntity entity) {
        JSONObject json = JSONUtil.parseObj(entity);
        //remove unused field
        json.remove("id");
        json.remove("partId");
        json.remove("salesVolume");
        json.remove("status");
        json.remove("md5");
        json.remove("updateTime");
        json.remove("createTime");
        String md5 = MD5.create().digestHex(json.toString()).toUpperCase();
        return md5;
    }
}

