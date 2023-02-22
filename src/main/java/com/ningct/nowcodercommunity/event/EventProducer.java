package com.ningct.nowcodercommunity.event;

import com.alibaba.fastjson2.JSONObject;
import com.ningct.nowcodercommunity.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class EventProducer {
    @Resource
    private KafkaTemplate kafkaTemplate;
    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
