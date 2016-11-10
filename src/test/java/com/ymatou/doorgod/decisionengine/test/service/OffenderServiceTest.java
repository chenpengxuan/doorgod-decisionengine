/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.service;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

/**
 * @author luoshiqian 2016/11/10 18:26
 */
public class OffenderServiceTest extends BaseTest{

    @Autowired
    private OffenderService offenderService;
    @Autowired
    private RuleDiscoverer ruleDiscoverer;


    @Test
    public void test(){
        ruleDiscoverer.execute();

        Sample sample = new Sample();
        sample.getDimensionValues().put("ip","192.168.0.6");

        //保存第一个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now(),LocalDateTime.now().format(FORMATTER_YMDHMS));

        //保存第二个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now().plusMinutes(10),LocalDateTime.now().plusMinutes(10).format(FORMATTER_YMDHMS));


        //保存第三个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now().plusMinutes(30),LocalDateTime.now().plusMinutes(30).format(FORMATTER_YMDHMS));
    }

}
