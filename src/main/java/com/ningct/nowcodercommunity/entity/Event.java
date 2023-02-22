package com.ningct.nowcodercommunity.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class Event {
    private String topic;//主题
    private int userId;//用户
    private int entityType;//实体类型
    private int entityId;//实体Id
    private int entityUserId;//实体作者Id
    private Map<String,Object> data = new HashMap<>();

    public Event setData(String key, int value) {
        data.put(key,value);
        return this;
    }
    public Event setData(String key, String value) {
        data.put(key,value);
        return this;
    }
}
