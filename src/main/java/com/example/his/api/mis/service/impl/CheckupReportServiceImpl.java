package com.example.his.api.mis.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.example.his.api.common.MinioUtil;
import com.example.his.api.common.OcrUtil;
import com.example.his.api.common.PageUtils;
import com.example.his.api.db.dao.AppointmentDao;
import com.example.his.api.db.dao.CheckupReportDao;
import com.example.his.api.db.dao.CheckupResultDao;
import com.example.his.api.db.pojo.CheckupResultEntity;
import com.example.his.api.exception.HisException;
import com.example.his.api.mis.service.CheckupReportService;
import com.example.his.api.report.CheckupReportUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class CheckupReportServiceImpl implements CheckupReportService {
    @Resource
    private CheckupReportDao checkupReportDao;

    @Resource
    private AppointmentDao appointmentDao;

    @Resource
    private CheckupResultDao checkupResultDao;

    @Resource
    private CheckupReportUtil checkupReportUtil;

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private OcrUtil ocrUtil;

    @Override
    public PageUtils searchByPage(Map param) {
        ArrayList<HashMap> list = new ArrayList<>();
        long count = checkupReportDao.searchCount(param);
        if (count > 0) {
            list = checkupReportDao.searchByPage(param);
        }
        int page = MapUtil.getInt(param, "page");
        int length = MapUtil.getInt(param, "length");
        PageUtils pageUtils = new PageUtils(list, count, page, length);

        return pageUtils;
    }

    @SneakyThrows
    @Override
    @Transactional
    public boolean createReport(Integer id) {
        HashMap result = checkupReportDao.searchById(id);
        if (result == null || result.size() == 0) {
            throw new HisException("存在しない健康診断報告書の記録です。");
        }
        int appointmentId = MapUtil.getInt(result, "appointmentId");
        String resultId = MapUtil.getStr(result, "resultId");
        int status = MapUtil.getInt(result, "status");
        DateTime date = DateUtil.parseDate(MapUtil.getStr(result, "date"));
        DateTime today = new DateTime(DateUtil.today());
        //check if in 10 days
//        if (today.offsetNew(DateField.DAY_OF_MONTH, -10).isBefore(date)) {
//            throw new HisException("cannot create report in 10 days");
//        }
        //check create report
        if (status != 1) {
            log.debug("Primary key " + id + ": report has created, task end.");
            return true;
        }

        HashMap map = appointmentDao.searchDataForReport(appointmentId);
        CheckupResultEntity entity = checkupResultDao.searchById(resultId);
        List<Map> checkup = entity.getCheckup();
        map.put("checkup", checkup);
        map.put("result", entity.getResult());

        HashSet set = new HashSet();

        checkup.forEach(one -> {
            String place = MapUtil.getStr(one, "place");
            set.add(place);
        });
        List<String> placeList = entity.getPlace();

        //if has blank, send mail manager
//        if (placeList.size() < set.size()) {
//            log.debug("Primary key " + id + ": has blank record.");
//            //TODO send mail or SMS
//            return false;
//        }

        //create report
        XWPFDocument report = checkupReportUtil.createReport(map);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        report.write(out);
        out.flush();
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        String filePath = "/report/checkup/" + resultId + ".docx";
        //upload report to minio
        minioUtil.uploadWord(filePath, in);

        //update report
        checkupReportDao.update(new HashMap() {{
            put("status", 2);
            put("filePath", filePath);
            put("id", id);
        }});

        return true;
    }

    @Override
    public HashMap identifyWaybill(String imgBase64) {
        try {
            HashMap map = ocrUtil.identifyWaybill(imgBase64);
            return map;
        } catch (Exception e) {
            log.error("OCR识别异常", e);
            throw new HisException("OCR识别异常");
        }
    }

    @Override
    @Transactional
    public boolean addWaybill(Map param) {
        int rows = checkupReportDao.update(param);
        return rows == 1;
    }

    @Override
    @Transactional
    public XSSFWorkbook importWaybills(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);

            ArrayList<HashMap> list = new ArrayList();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                String uuid = row.getCell(1).getStringCellValue();
                String recName = row.getCell(2).getStringCellValue();
                String recTel = row.getCell(3).getStringCellValue();
                String waybillCode = row.getCell(4).getStringCellValue();

                HashMap map = appointmentDao.searchDataForWaybill(uuid);
                if (map == null || map.size() == 0) {
                    list.add(new HashMap() {{
                        put("uuid", uuid);
                        put("recName", recName);
                        put("recTel", recTel);
                        put("waybillCode", waybillCode);
                        put("result", "体检编号错误");
                    }});
                    continue;
                }
                Integer appointmentId = MapUtil.getInt(map, "id");
                String name = MapUtil.getStr(map, "name");
                String tel = MapUtil.getStr(map, "tel");

                if (!recName.equals(name) || !recTel.equals(tel)) {
                    list.add(new HashMap() {{
                        put("uuid", uuid);
                        put("recName", recName);
                        put("recTel", recTel);
                        put("waybillCode", waybillCode);
                        put("result", "收件人姓名或者电话与体检人不符");
                    }});
                    continue;
                }

                HashMap param = new HashMap() {{
                    put("waybillCode", waybillCode);
                    put("waybillDate", DateUtil.today());
                    put("appointmentId", appointmentId);
                }};

                int rows = checkupReportDao.updateWaybill(param);
                if (rows == 0) {
                    list.add(new HashMap() {{
                        put("uuid", uuid);
                        put("recName", recName);
                        put("recTel", recTel);
                        put("waybillCode", waybillCode);
                        put("result", "更新失败");
                    }});
                }
            }

            if (list.size() == 0) {
                return null;
            }

            workbook = new XSSFWorkbook();

            XSSFCellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.MEDIUM);
            style.setBorderLeft(BorderStyle.MEDIUM);
            style.setBorderTop(BorderStyle.MEDIUM);
            style.setBorderRight(BorderStyle.MEDIUM);

            XSSFFont font = workbook.createFont();
            font.setFontName("微软雅黑");
            style.setFont(font);

            XSSFDataFormat dataFormat = workbook.createDataFormat();
            style.setDataFormat(dataFormat.getFormat("@"));

            sheet = workbook.createSheet("导入失败的运单");
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 7000);
            sheet.setColumnWidth(5, 7000);

            XSSFRow row = sheet.createRow(0);
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(style);

            XSSFColor color = new XSSFColor(new Color(255, 255, 0), new DefaultIndexedColorMap());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(color);

            XSSFCell cell = row.createCell(0);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("序号");

            cell = row.createCell(1);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("体检编号");

            cell = row.createCell(2);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("收件人");

            cell = row.createCell(3);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("收件人电话");

            cell = row.createCell(4);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("运单号码");

            cell = row.createCell(5);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("导入失败原因");

            for (int i = 1; i <= list.size(); i++) {
                Map map = list.get(i - 1);
                String waybillCode = MapUtil.getStr(map, "waybillCode");
                String uuid = MapUtil.getStr(map, "uuid");
                String recName = MapUtil.getStr(map, "recName");
                String recTel = MapUtil.getStr(map, "recTel");
                String result = MapUtil.getStr(map, "result");

                row = sheet.createRow(i);

                cell = row.createCell(0);
                cell.setCellStyle(style);
                cell.setCellValue(i);

                cell = row.createCell(1);
                cell.setCellStyle(style);
                cell.setCellValue(uuid);

                cell = row.createCell(2);
                cell.setCellStyle(style);
                cell.setCellValue(recName);

                cell = row.createCell(3);
                cell.setCellStyle(style);
                cell.setCellValue(recTel);

                cell = row.createCell(4);
                cell.setCellStyle(style);
                cell.setCellValue(waybillCode);

                cell = row.createCell(5);
                cell.setCellStyle(style);
                cell.setCellValue(result);
            }
            return workbook;

        } catch (Exception e) {
            log.error("運送伝票の更新に失敗しました。", e);
            throw new HisException("運送伝票の更新に失敗しました。");
        }
    }
}
