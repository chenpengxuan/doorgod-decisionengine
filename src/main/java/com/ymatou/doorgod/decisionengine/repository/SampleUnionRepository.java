/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * 
 * @author qianmin 2016年9月13日 下午2:48:03
 * 
 */
public interface SampleUnionRepository extends MongoRepository<TypedTuple<String>, String> {

}
