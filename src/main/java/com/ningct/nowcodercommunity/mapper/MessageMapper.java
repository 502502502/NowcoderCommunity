package com.ningct.nowcodercommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ningct.nowcodercommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    //查找用户的会话列表，每个会话返回一条最新的消息
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit);

    //查找用户的会话数量
    int selectConversationCount(@Param("userId")int userId);
    //删除所有
    int deleteallMessage();

    //查找某个会话的消息列表
    List<Message> selectLetters(@Param("conversationId")String conversationId, @Param("offset")int offset, @Param("limit")int limit);

    //查找某个会话未读消息数量
    int selectLetterUnreadCount(@Param("userId")int userId, @Param("conversationId")String conversationId);

    //查找某个会话的消息数量
    int selectLetterCount(@Param("conversationId")String conversationId);

    //插入新增消息
    int insertMessage(Message message);

    //修改消息状态
    int updateMessageStatus(@Param("ids") List<Integer> ids, @Param("status")int status);

    //查询某个主题的最新消息
    Message selectLastNotice(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题的消息数量
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题的未读消息数量
    int selectNoticeUnreadCount(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题的所有消息
    List<Message> selectNotics(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

}
