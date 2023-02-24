package com.ningct.nowcodercommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ningct.nowcodercommunity.entity.Comment;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    //查询某个实体对象的评论
    List<Comment> selectCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);
    //获取评论数量
    int selectCommentCount(@Param("entityType") int entityType, @Param("entityId") int entityId);
    //插入评论
    int insertComment(Comment comment);
    //查询指定评论
    Comment selectCommentById(@Param("id") int id);

    //获取指定用户的评论
    List<Comment> findCommentByUserId(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int findCommentCount(@Param("userId") int userId);
}
