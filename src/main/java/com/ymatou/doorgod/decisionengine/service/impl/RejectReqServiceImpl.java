/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHM;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ymatou.doorgod.decisionengine.model.mongo.RejectReqPo;
import com.ymatou.doorgod.decisionengine.repository.RejectReqRepository;
import com.ymatou.doorgod.decisionengine.service.RejectReqService;
import com.ymatou.doorgod.decisionengine.util.DateUtils;

import java.util.Date;

/**
 * @author luoshiqian 2016/9/26 14:40
 */
@Service
public class RejectReqServiceImpl implements RejectReqService {

    @Autowired
    private RejectReqRepository rejectReqRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveRejectReq(RejectReqPo rejectReqPo) {

        Index index = new Index("rejectTime", Sort.Direction.ASC);
        index.on("ruleName",Sort.Direction.ASC).on("sample",Sort.Direction.ASC);
        mongoTemplate.indexOps("RejectReq").ensureIndex(index);

        //格式化到分钟
        String rejectTime = DateUtils.parseAndFormat(rejectReqPo.getRejectTime(),FORMATTER_YMDHMS,FORMATTER_YMDHM);

        Query query = new Query(Criteria
                .where("rejectTime").is(rejectTime)
                .and("sample").is(rejectReqPo.getSample())
                .and("ruleName").is(rejectReqPo.getRuleName())
        );

        Update update = new Update();
        update.inc("count",1);
        update.set("addTime",new Date());

        mongoTemplate.findAndModify(query,update,new FindAndModifyOptions()
                .returnNew(true).upsert(true),RejectReqPo.class);
    }
}
