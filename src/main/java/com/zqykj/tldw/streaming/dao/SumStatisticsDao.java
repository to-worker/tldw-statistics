package com.zqykj.tldw.streaming.dao;

import com.zqykj.tldw.streaming.entity.SumStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by weifeng on 2018/5/7.
 */
public interface SumStatisticsDao extends MongoRepository<SumStatistics, String> {

    @Query("{'metricName':?0}")
    SumStatistics findByMetricName(String metricName);

}
