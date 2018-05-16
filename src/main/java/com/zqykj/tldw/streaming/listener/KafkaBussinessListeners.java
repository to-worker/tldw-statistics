package com.zqykj.tldw.streaming.listener;

import com.alibaba.fastjson.JSON;
import com.zqykj.tldw.streaming.common.Constants;
import com.zqykj.tldw.streaming.entity.BussinessStatistics;
import com.zqykj.tldw.streaming.entity.ElpTypeStatistics;
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
import java.util.HashMap;
import java.util.List;
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

    public KafkaBussinessListeners() {
    }

    @PostConstruct
    public void init() {
        sumStatisticsMap = new HashMap<>();
        bussinessStatisticsMap = new HashMap<>();
        initStatistics(Constants.METRICS_TASK_TRANS);
        initStatistics(Constants.METRICS_TASK_LOADER);
    }

    public void initStatistics(String metricName) {
        List<SumStatistics> statisticsList = sumStatisticsService.findByMetricName(metricName);
        logger.info("metric:{}, statisticsList.size: {}", metricName, statisticsList.size());

        if (null != statisticsList && statisticsList.size() > 0) {
            for (SumStatistics sumStatistics : statisticsList) {
                sumStatisticsMap
                        .put(sumStatistics.getMetricName() + Constants.METRICS_BUSSINESS_SEPERATOR + sumStatistics
                                .getTaskId(), sumStatistics);
            }
        }
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
        if (null == bussinessStatistics.getRecordTime()) {
            logger.error("recordTime cat not be null.");
            return;
        }
        if (null == bussinessStatistics.getMetricName() || null == bussinessStatistics.getTaskId()) {
            logger.error("metricName or taskId cat not be null.");
            return;
        }

        Date recordTime = bussinessStatistics.getRecordTime();
        String formatTime = DateUtils.getWholeMinute(recordTime);

        String metric = record.key().toString();
        String busMetricTaskId = metric + Constants.METRICS_BUSSINESS_SEPERATOR + bussinessStatistics.getTaskId();
        BussinessStatistics statisticsGather = bussinessStatisticsMap.get(busMetricTaskId);

        // 增加新的task指标
        if (null == statisticsGather) {
            statisticsGather = bussinessStatistics;
            statisticsGather.setRecordTime(new Date());
            bussinessStatisticsMap.put(busMetricTaskId, statisticsGather);
        }
        SumStatistics sumStatistics = sumStatisticsMap.get(busMetricTaskId);
        if (null == sumStatistics){
            sumStatistics = compactSumStatistics(bussinessStatistics, formatTime);
        }

        verifyRecords(statisticsGather, bussinessStatistics);
        updateSumStatistics(sumStatistics, bussinessStatistics);

        if (formatTime.equals(DateUtils.getWholeMinute(statisticsGather.getRecordTime()))) {
            updateValue(statisticsGather, bussinessStatistics);
        } else {
            // 每批次更新一次总值
            sumStatistics = sumStatisticsService.saveOrUpdate(sumStatistics);
            sumStatisticsMap.put(busMetricTaskId, sumStatistics);

            // 保存汇聚的结果
            bussinessStatisticsService.save(statisticsGather);
            logger.info("save bussinessStatistics: {}", statisticsGather);

            // 更新指标
            statisticsGather = compactBussStatistics(bussinessStatistics, formatTime);
            logger.info("put new bussinessStatistics: {}", statisticsGather);
            bussinessStatisticsMap.put(busMetricTaskId, statisticsGather);
        }

    }

    private void updateSumStatistics(SumStatistics sumStatistics, BussinessStatistics bussinessStatistics) {
        Long inTotal = sumStatistics.getInTotalRecords();
        Long outTotal = sumStatistics.getOutTotalRecords();
        if (null == inTotal) {
            inTotal = 0L;
        }
        if (null == outTotal) {
            outTotal = 0L;
        }
        sumStatistics.setInTotalRecords(inTotal + bussinessStatistics.getInTotalRecords());
        sumStatistics.setOutTotalRecords(outTotal + bussinessStatistics.getOutTotalRecords());
    }

    private void updateValue(BussinessStatistics statisticsGather, BussinessStatistics bussinessStatistics) {
        statisticsGather
                .setInTotalRecords(statisticsGather.getInTotalRecords() + bussinessStatistics.getInTotalRecords());
        statisticsGather
                .setOutTotalRecords(statisticsGather.getOutTotalRecords() + bussinessStatistics.getOutTotalRecords());
        //            List<ElpTypeStatistics> elpTypeStatisticsList = statisticsGather.getElpTypeStatistic();
        //            for (ElpTypeStatistics elpTypeCount: elpTypeStatisticsList){
        //            }
        statisticsGather.setEndTime(new Date());
    }

    private void verifyRecords(BussinessStatistics statisticsGather, BussinessStatistics bussinessStatistics) {
        Long inTotal = statisticsGather.getInTotalRecords();
        Long inTotalItem = bussinessStatistics.getInTotalRecords();
        Long outTotal = statisticsGather.getOutTotalRecords();
        Long outTotalItem = bussinessStatistics.getOutTotalRecords();
        if (null == inTotal) {
            statisticsGather.setInTotalRecords(0L);
        }
        if (null == inTotalItem) {
            bussinessStatistics.setInTotalRecords(0L);
        }
        if (null == outTotal) {
            statisticsGather.setOutTotalRecords(0L);
        }
        if (null == outTotalItem) {
            bussinessStatistics.setOutTotalRecords(0L);
        }
    }

    private BussinessStatistics compactBussStatistics(BussinessStatistics bussinessStatistics, String formatTime) {
        bussinessStatistics.setRecordTime(DateUtils.getDate(formatTime));
        return bussinessStatistics;
    }

    private SumStatistics compactSumStatistics(BussinessStatistics bussinessStatistics, String formatTime) {
        SumStatistics sumStatistics = new SumStatistics();
        sumStatistics.setId(bussinessStatistics.getTaskId());
        sumStatistics.setMetricName(bussinessStatistics.getMetricName());
        sumStatistics.setTaskId(bussinessStatistics.getTaskId());
        sumStatistics.setResId(bussinessStatistics.getResId());
        sumStatistics.setInTotalRecords(bussinessStatistics.getInTotalRecords());
        sumStatistics.setOutTotalRecords(bussinessStatistics.getOutTotalRecords());
        sumStatistics.setElpTypeStatistic(bussinessStatistics.getElpTypeStatistic());
        sumStatistics.setRecordTime(new Date());
        sumStatistics.setStartTime(new Date());
        sumStatistics.setEndTime(new Date());
        return sumStatistics;
    }

}