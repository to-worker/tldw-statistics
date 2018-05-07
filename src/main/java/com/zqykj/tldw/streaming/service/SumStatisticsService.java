package com.zqykj.tldw.streaming.service;

import com.zqykj.tldw.streaming.dao.SumStatisticsDao;
import com.zqykj.tldw.streaming.entity.SumStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by weifeng on 2018/5/7.
 */
@Service
public class SumStatisticsService {

    @Autowired
    private SumStatisticsDao sumStatisticsDao;

    public void save(SumStatistics sumStatistics) {
        sumStatisticsDao.save(sumStatistics);
    }

    public SumStatistics findByMetricName(String metricName) {
        return sumStatisticsDao.findByMetricName(metricName);
    }

    public void saveOrUpate(SumStatistics sumStatistics) {
        sumStatisticsDao.save(sumStatistics);
    }

}