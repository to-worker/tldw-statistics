package com.zqykj.tldw.streaming.service;

import com.zqykj.tldw.streaming.dao.BussinessStatisticsDao;
import com.zqykj.tldw.streaming.entity.BussinessStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by weifeng on 2018/5/7.
 */
@Service
public class BussinessStatisticsService {

    @Autowired
    private BussinessStatisticsDao bussinessStatisticsDao;

    public void save(BussinessStatistics statistics) {
        bussinessStatisticsDao.save(statistics);
    }

}
