package com.example.his.api.schedule;

import com.example.his.api.async.CheckupWorkAsync;
import com.example.his.api.db.dao.CheckupReportDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;

@Component
@Slf4j
public class CheckupReportSchedule {
    @Resource
    private CheckupReportDao checkupReportDao;

    @Resource
    private CheckupWorkAsync checkupWorkAsync;

    /**
     * 毎日午前1時から5時まで、20分ごとに10日前の健康診断報告書を作成します。
     */
    //@Scheduled(cron = "0 0,20,40 1,2,3,4,5 * * ?")
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void createReport() {
        ArrayList<Integer> list = checkupReportDao.searchWillGenerateReport();
        if (list == null || list.size() == 0) {
            return;
        }
        // Create checkup report by thread
        list.forEach(one -> {
            checkupWorkAsync.createReport(one);
        });
        log.debug(list.size() + "件の健康診断レポートを生成しました。");
    }
}
