package com.ningct.nowcodercommunity.controller;


import com.ningct.nowcodercommunity.entity.Comment;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.entity.Event;
import com.ningct.nowcodercommunity.entity.Page;
import com.ningct.nowcodercommunity.event.EventProducer;
import com.ningct.nowcodercommunity.service.CommentService;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.HostHolder;
import com.ningct.nowcodercommunity.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant {
    @Resource
    private CommentService commentService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)

    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment, Page page){
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.getUser().getId());
        commentService.addComment(comment);
        System.out.println(page.getCurrent());
        //创建评论事件
        Event event = new Event();
        //设置主题
        event.setTopic(TOPIC_COMMENT);
        //设置触发类型
        event.setEntityType(comment.getEntityType());
        //设置触发对象
        event.setUserId(hostHolder.getUser().getId());
        //设置触发内容
        event.setEntityId(comment.getEntityId());
        //设置事件内容
        event.setData("postId",discussPostId);
        //设置接收对象
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());

            //更新帖子分数
            String key = RedisKeyUtil.getScorePostRefreshKey();
            redisTemplate.opsForSet().add(key,discussPostId);

        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);


        //评论事件
        Event event1 = new Event();
        event1.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event1.setEntityType(ENTITY_TYPE_POST);
        event1.setEntityId(discussPostId);
        eventProducer.fireEvent(event1);

        return "redirect:/discuss/detail/" +discussPostId+"?current="+page.getCurrent();
    }
}
