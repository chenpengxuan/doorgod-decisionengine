/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.*;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.model.mongo.OffenderPo;
import com.ymatou.doorgod.decisionengine.repository.OffenderRepository;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.KafkaClients;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:04:36
 * 
 */
@Component
public class MongoSampleOffendersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(MongoSampleOffendersJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
            KafkaClients kafkaClients = SpringContextHolder.getBean(KafkaClients.class);
            BizProps bizProps = SpringContextHolder.getBean(BizProps.class);

            OffenderService offenderService = SpringContextHolder.getBean(OffenderService.class);
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            // 被统计的规则
            String jobName = context.getJobDetail().getKey().getName();
            LimitTimesRule rule = RuleHolder.rules.get(jobName);
            if(null == rule){
                logger.info("exec offenders job :{} rule==null",jobName);
                return;
            }
            String ruleName = rule.getName();

            logger.info("exec offenders job :{}",ruleName);

            // 合并Redis涉及到的时间窗口
            LocalDateTime now = LocalDateTime.now();
            String nowFormated = now.format(FORMATTER_YMDHMS);

            List<String> timeBuckets = getAllTimeBucket(rule, now);
            String previousBucket = getUnionSetName(ruleName, now.minusSeconds(1).format(FORMATTER_YMDHMS), UNION);
            String currentBucket = getUnionSetName(ruleName, now.format(FORMATTER_YMDHMS), UNION);
            String secAdd = getNormalSetName(ruleName, now.minusSeconds(1).format(FORMATTER_YMDHMS));
            String secDel = getNormalSetName(ruleName,
                    now.minusSeconds(rule.getStatisticSpan() + 1).format(FORMATTER_YMDHMS));
            logger.info("begin to execute rule:{},time:{},previousBucket:{},currentBucket:{},secAdd:{},secDelete:{}",
                    ruleName, now.format(FORMATTER_YMDHMS), previousBucket, currentBucket, secAdd, secDel);

            int previousSeconds = bizProps.getPreviousSecondsRedisSkip();
            String previousNSecondsUnionTimeBucket = getPreviousNSecondsUnionTimeBucket(rule, now, zSetOps,previousSeconds);//获取10秒前的union 合集
            if (StringUtils.isNotBlank(previousNSecondsUnionTimeBucket)) { // 存在合并后的时间窗口
                LocalDateTime time = LocalDateTime.parse(previousNSecondsUnionTimeBucket.split(":")[3], FORMATTER_YMDHMS);

                String addTimeBucket = getUnionSetName(ruleName, time.format(FORMATTER_YMDHMS), "UnionAdd");
                String delTimeBucket = getUnionSetName(ruleName, time.format(FORMATTER_YMDHMS), "UnionDelete");
                List<String> delSecTimeBuckets = getAllDeleteSecTimeBucket(rule, previousSeconds, now);
                List<String> addSecTimeBuckets = getAllAddSecTimeBucket(rule, previousSeconds, now);

                logger.info(
                        "union any.Now:{}, previousNSecondsUnionTimeBucket:{}, delSecTimeBuckets:{}, addSecTimeBuckets: {}",
                        now.format(FORMATTER_YMDHMS), previousNSecondsUnionTimeBucket, JSON.toJSONString(delSecTimeBuckets),
                        JSON.toJSONString(addSecTimeBuckets));

                zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), delSecTimeBuckets, delTimeBucket); // 合并需要被删除的时间窗口
                zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), addSecTimeBuckets, addTimeBucket); // 合并需要被添加的时间窗口

                redisTemplate.execute(new RedisCallback<Long>() {
                    @Override
                    public Long doInRedis(RedisConnection connection) throws DataAccessException {
                        // 合并需要被删除的， 被添加的， 已经合并的
                        byte[][] sets = {delTimeBucket.getBytes(),previousNSecondsUnionTimeBucket.getBytes(),addTimeBucket.getBytes()};
                        int weight[] = {-1,1,1};
                        return connection.zUnionStore(currentBucket.getBytes(), RedisZSetCommands.Aggregate.SUM,weight,sets);
                    }
                });

                zSetOps.getOperations().expire(addTimeBucket, 1, TimeUnit.SECONDS);
                zSetOps.getOperations().expire(delTimeBucket, 1, TimeUnit.SECONDS);
                zSetOps.getOperations().expire(currentBucket, getExpireByRule(rule), TimeUnit.SECONDS);
            } else {// 不存在合并后的时间窗口
                // 合并所有的子时间窗口
                zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), timeBuckets, currentBucket);
                zSetOps.getOperations().expire(currentBucket, getExpireByRule(rule), TimeUnit.SECONDS);
            }

            // 获取Offender
            Set<String> offenders = zSetOps.rangeByScore(currentBucket, rule.getTimesCap(), Integer.MAX_VALUE);
            if (!offenders.isEmpty()) {
                zSetOps.removeRangeByScore(currentBucket, rule.getTimesCap(), Double.MAX_VALUE);
                String releaseDate = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);

                boolean isOffendersChanged = false;
                for (String offender : offenders) {
                    if(offenderService.saveOffender(rule,offender,releaseDate,nowFormated)){
                        isOffendersChanged = true;
                    }
                }
                if (isOffendersChanged) {

                    kafkaClients.sendUpdateOffendersEvent(ruleName);
                    logger.info("got offenders: {}", JSON.toJSONString(offenders));
                }
            }
            logger.info("end to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
        } catch (Exception e) {
            logger.error("error execute rule:{}",context.getJobDetail().getKey().getName(),e);
        }
    }


    /**
     * 每一分钟持久化一次Offender
     *
     * @param rule
     * @param now
     */
    public void persistOffender(LimitTimesRule rule, LocalDateTime now) {
        // 持久化Offender
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
        OffenderRepository offenderRepository = SpringContextHolder.getBean(OffenderRepository.class);
        Set<ZSetOperations.TypedTuple<String>> blackList =
                redisTemplate.opsForZSet().rangeWithScores(getOffendersMapName(rule.getName()), 0, -1);
        if (blackList != null && !blackList.isEmpty()) {
            List<OffenderPo> offenderPos = new ArrayList<>();
            for (ZSetOperations.TypedTuple<String> typeTuple : blackList) {
                OffenderPo offenderPo = new OffenderPo();
                offenderPo.setRuleName(rule.getName());
                offenderPo.setSample(typeTuple.getValue());
                offenderPo.setReleaseDate(typeTuple.getScore().longValue());

                // 查询MongoDB中是否已经存在， 若不存在则保存
                Example<OffenderPo> example = Example.of(offenderPo);
                OffenderPo result = offenderRepository.findOne(example);
                if (result == null) {
                    offenderPo.setAddTime(now.format(FORMATTER_YMDHMS));
                    offenderPos.add(offenderPo);
                }
            }
            offenderRepository.save(offenderPos);
            logger.info("persist offender size: {}, rule: {}", offenderPos.size(), rule.getName());
        }

        // 删除到期的Offender
        redisTemplate.opsForZSet().removeRangeByScore(getOffendersMapName(rule.getName()), 0,
                Double.valueOf(LocalDateTime.now().format(FORMATTER_YMDHMS)));
    }


    /**
     * 往前N秒，获取已合并的时间窗口
     *
     * @param rule
     * @param now
     * @param zSetOps
     * @return
     */
    private String getPreviousNSecondsUnionTimeBucket(LimitTimesRule rule, LocalDateTime now,ZSetOperations<String, String> zSetOps,int previousSeconds) {
        String previousNBucket = getUnionSetName(rule.getName(), now.minusSeconds(previousSeconds).format(FORMATTER_YMDHMS), UNION);
        if (zSetOps.size(previousNBucket) > 0) {
            return previousNBucket;
        }
        return null;
    }

    /**
     * 往前找N秒，找到最近已合并的时间窗口
     * 
     * @param rule
     * @param now
     * @param zSetOps
     * @return
     */
    private String getNoEmptyUnionTimeBucket(LimitTimesRule rule, LocalDateTime now,
            ZSetOperations<String, String> zSetOps) {
        List<String> unionTimeBuckets = getPreviousUionTimeBucket(rule, now);
        String notEmptyUnionTimeBucket = null;
        for (String utb : unionTimeBuckets) {
            if (zSetOps.size(utb) > 0) {
                notEmptyUnionTimeBucket = utb; // 找到已经合并的时间窗口
                break;
            }
        }

        return notEmptyUnionTimeBucket;
    }

    /**
     * 找到所以需要合并的时间窗口
     * 
     * @param rule
     * @param now
     * @return
     */
    private List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan(); second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private List<String> getPreviousUionTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = 1; second <= PREVIOUS_COUNT; second++) {
            timeBuckets.add(getUnionSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS), UNION));
        }
        return timeBuckets;
    }

    private List<String> getAllDeleteSecTimeBucket(LimitTimesRule rule, int seconds, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan() + seconds; second > rule.getStatisticSpan(); second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private List<String> getAllAddSecTimeBucket(LimitTimesRule rule, int seconds, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = seconds; second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private long getExpireByRule(LimitTimesRule rule) {
        if (rule.getTimesCap() < 60) {
            return ((Double) (rule.getStatisticSpan() * 1.5)).longValue();
        }
        return ((Double) (rule.getStatisticSpan() * 1.2)).longValue();
    }
}
