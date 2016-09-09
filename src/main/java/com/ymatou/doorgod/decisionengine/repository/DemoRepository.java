/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.repository;

import com.ymatou.doorgod.decisionengine.model.Demo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author luoshiqian 2016/9/9 15:39
 */
@Repository
public interface DemoRepository extends MongoRepository<Demo,String>{
}
