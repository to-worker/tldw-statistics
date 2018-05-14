package com.zqykj.tldw.streaming.dao;

import com.zqykj.tldw.streaming.entity.SumStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by weifeng on 2018/5/7.
 */
public interface SumStatisticsDao extends MongoRepository<SumStatistics, String> {

    @Query("{'metricName':?0}")
    List<SumStatistics> findByMetricName(String metricName);

    @Query("{'metricName':?0, 'taskId':?1, 'resId':?2}")
    SumStatistics findByMetricAndTaskIdAndResId(String metricName, String taskId, String resId);

}
