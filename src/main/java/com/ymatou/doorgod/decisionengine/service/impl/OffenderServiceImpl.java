/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
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

/**
 * @author luoshiqian 2016/9/22 15:39
 */
@Service
public class OffenderServiceImpl implements OffenderService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Boolean saveOffender(LimitTimesRule rule, String sample, String releaseDate, String addTime) {

        // 查询MongoDB中是否已经存在， 若不存在则保存
        Query query = new Query(Criteria.where("ruleName").is(rule.getName())
                .and("sample").is(sample)
                .and("releaseDate").gt(Long.valueOf(addTime))
        );

        if (!mongoTemplate.collectionExists("LimitTimesRuleOffender")) {
            mongoTemplate.createCollection("LimitTimesRuleOffender", Constants.COLLECTION_OPTIONS);

            Index index = new Index("addTime", Sort.Direction.ASC);
            index.on("ruleName",Sort.Direction.ASC);
            mongoTemplate.indexOps("LimitTimesRuleOffender").ensureIndex(index);
        }

        if(!mongoTemplate.exists(query,OffenderPo.class)){
            Update update = new Update();
            update.set("ruleName",rule.getName());
            update.set("releaseDate",Long.valueOf(releaseDate));
            update.set("addTime",Long.valueOf(addTime));
            update.set("sample",sample);

            mongoTemplate.findAndModify(query,update,new FindAndModifyOptions()
                    .returnNew(true).upsert(true),OffenderPo.class);

            return true;
        }
        return false;
    }
}
