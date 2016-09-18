/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ymatou.doorgod.decisionengine.model.MongoGroupBySamplePo;

/**
 * @author luoshiqian 2016/9/9 15:39
 */
@Repository
public interface MongoGroupBySampleRepository extends MongoRepository<MongoGroupBySamplePo,String>{




}
