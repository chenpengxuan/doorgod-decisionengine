/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ymatou.doorgod.decisionengine.model.mongo.OffenderPo;


/**
 * 
 * @author qianmin 2016年9月14日 下午7:04:10
 * 
 */
@Repository
public interface OffenderRepository extends MongoRepository<OffenderPo, String> {

}
