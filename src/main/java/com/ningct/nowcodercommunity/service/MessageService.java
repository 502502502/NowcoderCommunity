package com.ningct.nowcodercommunity.service;

import com.ningct.nowcodercommunity.entity.Message;
import com.ningct.nowcodercommunity.mapper.MessageMapper;
import com.ningct.nowcodercommunity.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private SensitiveFilter filter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    //查询会话数量
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    //查询会话的所有消息
    public List<Message> findLetters(String conversationId, int offset, int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    //查询某个会话的消息数
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    //查询指定会话的未读消息数
    public int findLetterUnReadCount(int userId, String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    //查询最新一条通知
    public Message findLastNotice(int userId, String topic){
        return messageMapper.selectLastNotice(userId,topic);
    }

    //查询通知的数量
    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    //查询未读通知数量
    public int findNoticeUnreadCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    //查询所有通知
    public List<Message> findNotices(int userId, String topic, int offset, int limit){
        return messageMapper.selectNotics(userId, topic, offset, limit);
    }

    //插入通知
    public int addMessage(Message message){
        if(message == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent((filter.filter(message.getContent())));
        return messageMapper.insertMessage(message);
    }

    //已读消息
    public int readMessage(List<Integer> ids){
        return messageMapper.updateMessageStatus(ids,1);
    }

}
