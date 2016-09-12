/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.po;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author luoshiqian 2016/9/12 17:04
 */
@Entity
@Table(name = "uri_key_alias")
public class UriKeyAliasPo extends Audit {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uri")
    private String uri;

    @Column(name = "alias")
    private String alias;

    @Column(name = "origin_key_name")
    private String originKeyName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getOriginKeyName() {
        return originKeyName;
    }

    public void setOriginKeyName(String originKeyName) {
        this.originKeyName = originKeyName;
    }
}
