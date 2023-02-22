package com.ningct.nowcodercommunity.controller;


import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.entity.Page;
import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import com.ningct.nowcodercommunity.service.LikeService;
import com.ningct.nowcodercommunity.service.UserService;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Resource
    private UserService userService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;

    @RequestMapping(path = {"/index","/"}, method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode",defaultValue = "0") int orderMode) {
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" +orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                //帖子作者
                map.put("user", user);
                //点赞数
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }
}
