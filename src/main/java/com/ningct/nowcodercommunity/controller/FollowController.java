package com.ningct.nowcodercommunity.controller;


import com.ningct.nowcodercommunity.entity.Event;
import com.ningct.nowcodercommunity.entity.Page;
import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.event.EventProducer;
import com.ningct.nowcodercommunity.service.FollowerService;
import com.ningct.nowcodercommunity.service.UserService;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import com.ningct.nowcodercommunity.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private FollowerService followerService;
    @Resource
    private UserService userService;
    @Resource
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followerService.follow(user.getId(),entityType,entityId);

        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW);
        event.setUserId(user.getId());
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followerService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注！");
    }

    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该账号不存在！");
        }
        model.addAttribute("user",user);
        page.setPath("/followees/" +userId);
        page.setLimit(5);
        page.setRows((int)followerService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followerService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该账号不存在！");
        }
        model.addAttribute("user",user);
        page.setPath("/followers/" +userId);
        page.setLimit(5);
        page.setRows((int)followerService.findFollowerCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followerService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    public boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followerService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }
}
