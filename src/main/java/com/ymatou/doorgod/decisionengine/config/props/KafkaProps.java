/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config.props;

import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

/**
 * 
 * @author qianmin 2016年9月9日 下午3:57:06
 * 
 */
@Component
@DisconfFile(fileName = "kafka.properties")
public class KafkaProps {

    private String bootstrapServers;
    private String groupId;

    //topic
    private String statisticSampleTopic;
    private String updateOffendersTopic;
    private String updateRuleTopic;

    private int statisticSampleThreadNums;


    @DisconfFileItem(name = "kafka.bootstrapServers")
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @DisconfFileItem(name = "kafka.groupId")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @DisconfFileItem(name = "kafka.topic.statisticSampleEvent")
    public String getStatisticSampleTopic() {
        return statisticSampleTopic;
    }

    public void setStatisticSampleTopic(String statisticSampleTopic) {
        this.statisticSampleTopic = statisticSampleTopic;
    }

    @DisconfFileItem(name = "kafka.topic.updateOffendersEvent")
    public String getUpdateOffendersTopic() {
        return updateOffendersTopic;
    }

    public void setUpdateOffendersTopic(String updateOffendersTopic) {
        this.updateOffendersTopic = updateOffendersTopic;
    }

    @DisconfFileItem(name = "kafka.topic.updateRuleEvent")
    public String getUpdateRuleTopic() {
        return updateRuleTopic;
    }

    public void setUpdateRuleTopic(String updateRuleTopic) {
        this.updateRuleTopic = updateRuleTopic;
    }

    @DisconfFileItem(name = "kafka.consumer.statisticSampleThreadNums")
    public int getStatisticSampleThreadNums() {
        return statisticSampleThreadNums;
    }

    public void setStatisticSampleThreadNums(int statisticSampleThreadNums) {
        this.statisticSampleThreadNums = statisticSampleThreadNums;
    }

}
