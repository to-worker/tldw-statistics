package com.zqykj.tldw.streaming.listener;

import com.alibaba.fastjson.JSON;
import com.zqykj.tldw.streaming.common.Constants;
import com.zqykj.tldw.streaming.entity.BussinessStatistics;
import com.zqykj.tldw.streaming.entity.SumStatistics;
import com.zqykj.tldw.streaming.service.BussinessStatisticsService;
import com.zqykj.tldw.streaming.service.SumStatisticsService;
import com.zqykj.tldw.util.DateUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;

@Service
public class KafkaBussinessListeners {

    private static Logger logger = LoggerFactory.getLogger(KafkaBussinessListeners.class);

    @Value("${spring.kafka.statistics.topic}")
    private String topics;

    @Autowired
    private BussinessStatisticsService bussinessStatisticsService;

    @Autowired
    private SumStatisticsService sumStatisticsService;

    private Map<String, SumStatistics> sumStatisticsMap;

    private Map<String, BussinessStatistics> bussinessStatisticsMap;

    public KafkaBussinessListeners(Map<String, SumStatistics> sumStatisticsMap,
            Map<String, BussinessStatistics> bussinessStatisticsMap) {
        this.sumStatisticsMap = sumStatisticsMap;
        this.bussinessStatisticsMap = bussinessStatisticsMap;
    }

    @PostConstruct
    public void init() {
        SumStatistics transSum = sumStatisticsService.findByMetricName(Constants.METRICS_TRANS);
        if (null == transSum) {
            transSum = new SumStatistics();
            transSum.setMetricName(Constants.METRICS_TRANS);
            transSum.setInTotalRecords(0L);
            transSum.setOutTotalRecords(0L);
            transSum.setRecordTime(new Date());
            transSum.setStartTime(new Date());
        }

        SumStatistics loaderSum = sumStatisticsService.findByMetricName(Constants.METRICS_LOADER);
        if (null == loaderSum) {
            loaderSum = new SumStatistics();
            loaderSum.setMetricName(Constants.METRICS_LOADER);
            loaderSum.setInTotalRecords(0L);
            loaderSum.setOutTotalRecords(0L);
            loaderSum.setRecordTime(new Date());
            loaderSum.setStartTime(new Date());
        }
        sumStatisticsMap.put(Constants.METRICS_TRANS, transSum);
        sumStatisticsMap.put(Constants.METRICS_LOADER, loaderSum);
    }

    /**
     * 消费kafka数据，对一分钟内的消息进行汇聚:
     * 一分钟取整一分钟：00 - 59
     *
     * @param record
     */
    @KafkaListener(topics = { "Statistics" })
    public void listen(ConsumerRecord<?, ?> record) {

        BussinessStatistics bussinessStatistics = JSON
                .parseObject(record.value().toString(), BussinessStatistics.class);
        bussinessStatistics.getRecordTime();

        String metric = record.key().toString();
        BussinessStatistics statistics = bussinessStatisticsMap.get(metric);

        Date recordTime = bussinessStatistics.getRecordTime();
        String formatTime = DateUtils.getWholeMinute(recordTime);

        if (statistics.getRecordTime() != null) {
            if (formatTime.equals(DateUtils.getWholeMinute(statistics.getRecordTime()))) {
                statistics.setInTotalRecords(statistics.getInTotalRecords() + bussinessStatistics.getInTotalRecords());
                statistics
                        .setOutTotalRecords(statistics.getOutTotalRecords() + bussinessStatistics.getOutTotalRecords());
                statistics.setEndTime(new Date());
            } else {
                SumStatistics sumStatistics = sumStatisticsMap.get(metric);
                sumStatistics.setInTotalRecords(sumStatistics.getInTotalRecords() + statistics.getInTotalRecords());
                sumStatistics.setOutTotalRecords(sumStatistics.getOutTotalRecords() + statistics.getOutTotalRecords());
                sumStatistics.setRecordTime(new Date());
                logger.info("sumStatistisc: {}", sumStatistics);
                logger.info("bussinessStatistics: {}", statistics);

                sumStatisticsService.saveOrUpate(sumStatistics);
                bussinessStatisticsService.save(statistics);
                statistics = compactSumStatistics(bussinessStatistics, formatTime);
                bussinessStatisticsMap.put(metric, statistics);
            }
        } else {
            statistics = compactSumStatistics(bussinessStatistics, formatTime);
            bussinessStatisticsMap.put(metric, statistics);
        }

    }

    private BussinessStatistics compactSumStatistics(BussinessStatistics bussinessStatistics, String formatTime) {
        BussinessStatistics statistics = new BussinessStatistics();
        statistics.setMetricName(bussinessStatistics.getMetricName());
        statistics.setInTotalRecords(bussinessStatistics.getInTotalRecords());
        statistics.setOutTotalRecords(bussinessStatistics.getOutTotalRecords());
        statistics.setRecordTime(DateUtils.getDate(formatTime));
        statistics.setEndTime(new Date());
        statistics.setResId(bussinessStatistics.getResId());
        statistics.setElpTypeStatistic(bussinessStatistics.getElpTypeStatistic());
        return statistics;
    }

}