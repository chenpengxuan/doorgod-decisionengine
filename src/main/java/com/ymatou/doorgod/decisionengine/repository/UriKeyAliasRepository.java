/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ymatou.doorgod.decisionengine.model.po.UriKeyAliasPo;

/**
 * 
 * @author qianmin 2016年9月12日 下午6:21:20
 * 
 */
@Repository
public interface UriKeyAliasRepository extends JpaRepository<UriKeyAliasPo, Long> {

}
