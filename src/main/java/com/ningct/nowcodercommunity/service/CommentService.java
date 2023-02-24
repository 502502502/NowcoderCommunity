package com.ningct.nowcodercommunity.service;

import com.ningct.nowcodercommunity.entity.Comment;
import com.ningct.nowcodercommunity.mapper.CommentMapper;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private SensitiveFilter filter;
    @Resource
    private DiscussPostService discussPostService;

    //通过实体对象id查询评论
    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    //通过实体对象查询评论
    public List<Comment> findCommentsByEntity(int entityType, int enityId, int offset, int limid){
        return commentMapper.selectCommentByEntity(entityType, enityId, offset, limid);
    }

    //通过用户查询评论
    public List<Comment> findCommentByUserId(int userId, int offset, int limid){
        return commentMapper.findCommentByUserId(userId,offset,limid);
    }

    //查找评论的帖子id
    public int findPostId(int id){
        Comment comment = findCommentById(id);
        while(comment.getEntityType() != 1){
            comment = findCommentById(comment.getEntityId());
        }
        return comment.getEntityId();
    }

    //查询实体对象的评论的数量
    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCommentCount(entityType, entityId);
    }

    //查询用户的评论的数量
    public int findCommentCount(int userId){
        return commentMapper.findCommentCount(userId);
    }

    //插入评论
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw  new IllegalArgumentException("参数不能为空！");
        }
        //过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(filter.filter(comment.getContent()));
        //插入评论
        int rows = commentMapper.insertComment(comment);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCommentCount(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }


}
