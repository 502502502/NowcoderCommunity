package com.ningct.nowcodercommunity;

import com.ningct.nowcodercommunity.entity.Event;
import com.ningct.nowcodercommunity.event.EventProducer;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import javax.annotation.Resource;

@SpringBootTest
public class KafkaTest implements CommunityConstant {
    @Resource
    private EventProducer eventProducer;

    @Test
    public void testEventKafka(){
        Event event = new Event();
        event.setTopic(TOPIC_LIKE);
        event.setUserId(11111);
        event.setEntityType(-1);
        eventProducer.fireEvent(event);
    }
}