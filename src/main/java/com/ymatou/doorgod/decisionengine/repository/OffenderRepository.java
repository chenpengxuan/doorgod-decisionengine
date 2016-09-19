/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ymatou.doorgod.decisionengine.model.OffenderPo;


/**
 * 
 * @author qianmin 2016年9月14日 下午7:04:10
 * 
 */
public interface OffenderRepository extends MongoRepository<OffenderPo, String> {

}
