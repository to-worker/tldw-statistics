package com.zqykj.tldw.streaming.dao;

import com.zqykj.tldw.streaming.entity.BussinessStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by weifeng on 2018/5/7.
 */
public interface BussinessStatisticsDao extends MongoRepository<BussinessStatistics, String> {

}
