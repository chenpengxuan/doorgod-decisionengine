/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.mongo.OffenderPo;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.ymatou.doorgod.decisionengine.constants.Constants.DATE_FORMAT_YMD;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

/**
 * @author luoshiqian 2016/9/22 15:39
 */
@Service
public class OffenderServiceImpl implements OffenderService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Boolean saveOffender(LimitTimesRule rule, Sample sample, LocalDateTime now, String addTime) {

        // 查询MongoDB中是否已经存在， 若不存在则保存
        Query query = new Query(Criteria.where("ruleName").is(rule.getName())
                .and("releaseDate").gt(Long.valueOf(addTime))
                .and("sample").is(sample)
        );

        if (!mongoTemplate.collectionExists(Constants.COLLECTION_NAME_LIMIT_TIMES_RULE_OFFENDER)) {
            mongoTemplate.createCollection(Constants.COLLECTION_NAME_LIMIT_TIMES_RULE_OFFENDER, Constants.COLLECTION_OPTIONS);

            mongoTemplate.indexOps(Constants.COLLECTION_NAME_LIMIT_TIMES_RULE_OFFENDER).ensureIndex(new Index("addTime", Sort.Direction.ASC));

            mongoTemplate.indexOps(Constants.COLLECTION_NAME_LIMIT_TIMES_RULE_OFFENDER).ensureIndex(new Index("ruleName", Sort.Direction.ASC).on("releaseDate", Sort.Direction.ASC));
        }

        if(!mongoTemplate.exists(query,OffenderPo.class)){
            Update update = new Update();
            update.set("ruleName",rule.getName());
            update.set("releaseDate", Long.valueOf(getReleaseDate(rule, now,addTime,sample)));
            update.set("addTime",Long.valueOf(addTime));
            update.set("sample",sample);

            mongoTemplate.findAndModify(query,update,new FindAndModifyOptions()
                    .returnNew(true).upsert(true),OffenderPo.class);

            return true;
        }
        return false;
    }

    private String getReleaseDate(LimitTimesRule rule, LocalDateTime now, String addTime,Sample sample) {
        long todayStartTime = Long.valueOf(now.format(DATE_FORMAT_YMD) + "000000");
        long todayEndTime = Long.valueOf(addTime);


        Query query = new Query(Criteria.where("addTime").gte(todayStartTime)
                .andOperator(Criteria.where("addTime").lte(todayEndTime))
                .and("ruleName").is(rule.getName())
                .and("sample").is(sample)
        );

        long times = mongoTemplate.count(query, OffenderPo.class) + 1;

        // math.pow 计算乘方,下面是计算3的2次方 Math.pow(3,2);  /60 转化为分钟
        return now.plusSeconds(LongMath.pow(rule.getRejectionSpan()/60,(int)times) * 60).format(FORMATTER_YMDHMS);
    }
}
