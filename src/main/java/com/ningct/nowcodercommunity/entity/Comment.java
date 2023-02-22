package com.ningct.nowcodercommunity.entity;

import lombok.Data;

import java.util.Date;
@Data
public class Comment {
    private int id;
    private int userId; // 评论人的id
    private int entityType; // 针对谁进行评论 1：帖子 2：评论
    private int entityId; // 评论对象的id  针对帖子的评论：帖子的id  针对评论的评论：评论的id
    private int targetId; // a回复b，针对评论的评论，评论对象b的user_id，方便前端快速展示b的用户名
    private String content;
    private int status;
    private Date createTime;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
