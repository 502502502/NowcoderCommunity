package com.ningct.nowcodercommunity.controller;

import com.ningct.nowcodercommunity.entity.*;
import com.ningct.nowcodercommunity.event.EventProducer;
import com.ningct.nowcodercommunity.service.CommentService;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import com.ningct.nowcodercommunity.service.LikeService;
import com.ningct.nowcodercommunity.service.UserService;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import com.ningct.nowcodercommunity.util.HostHolder;
import com.ningct.nowcodercommunity.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private LikeService likeService;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate redisTemplate;


    //发布帖子
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"您还未登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityId(post.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //更新帖子分数
        String key = RedisKeyUtil.getScorePostRefreshKey();
        redisTemplate.opsForSet().add(key,post.getId());

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    //置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussostTop(int postId){
        discussPostService.updatePostType(postId, 1);

        //发帖事件，更新ES
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setEntityId(postId);
        event.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"置顶成功！");
    }

    //加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussostWoderful(int postId){
        discussPostService.updatePostStatus(postId, 1);

        //发帖事件，更新ES
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setEntityId(postId);
        event.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //更新帖子分数
        String key = RedisKeyUtil.getScorePostRefreshKey();
        redisTemplate.opsForSet().add(key,postId);

        return CommunityUtil.getJSONString(0,"加精成功！");
    }

    //删除
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteDiscussost(int postId){
        discussPostService.updatePostStatus(postId, 2);
        //发帖事件，更新ES
        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setEntityId(postId);
        event.setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"删除成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDisCussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //找帖子
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //找作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //获取用户
        User hostHolderUser = hostHolder.getUser();

        //点赞数
        model.addAttribute("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId));
        //点赞状态
        int state = 0;
        if(hostHolderUser != null) {
           state = likeService.findEntityLikeStatus(hostHolderUser.getId(), ENTITY_TYPE_POST, discussPostId);
        }
        model.addAttribute("likeStatus",state);
        //分页
        page.setPath("/discuss/detail/" +discussPostId);
        page.setRows(commentService.findCommentCount(ENTITY_TYPE_POST,discussPostId));
        page.setLimit(5);
        //查询帖子的回复列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,discussPostId,page.getOffset(),page.getLimit());
        //每个评论用一个map封装信息
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                //评论本身
                commentVo.put("comment",comment);
                //评论作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //评论的赞
                commentVo.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId()));
                //评论的赞状态
                state = 0;
                if(hostHolderUser != null) {
                    state = likeService.findEntityLikeStatus(hostHolderUser.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                }
                commentVo.put("likeStatus",state);
                //评论的回复
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE );
                //对评论的每个回复进行信息封装
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for (Comment reply : replyList) {
                        Map<String ,Object> replyVo = new HashMap<>();
                        //回复本身
                        replyVo.put("reply",reply);
                        //回复的作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复的人
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //回复的赞数量
                        replyVo.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId()));
                        //回复的赞状态
                        state = 0;
                        if(hostHolderUser != null) {
                            state = likeService.findEntityLikeStatus(hostHolderUser.getId(),
                                    ENTITY_TYPE_COMMENT, reply.getId());
                        }
                        replyVo.put("likeStatus",state);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);

                //将包装好的评论信息加入评论集合
                commentVoList.add(commentVo);
            }
        }
        //将评论信息列表响应
        model.addAttribute("comments", commentVoList);
        //跳转
        return "/site/discuss-detail";
    }
}
