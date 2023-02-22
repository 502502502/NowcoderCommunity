package com.ningct.nowcodercommunity.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", syatus=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
