/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service;

import com.ymatou.doorgod.decisionengine.model.RejectReqEvent;

/**
 * @author luoshiqian 2016/9/22 15:37
 */
public interface RejectReqService {

    void saveRejectReq(RejectReqEvent rejectReqEvent);

}
