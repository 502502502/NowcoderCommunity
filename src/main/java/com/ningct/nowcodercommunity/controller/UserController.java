package com.ningct.nowcodercommunity.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.PolicyConditions;

import com.ningct.nowcodercommunity.entity.Comment;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.entity.Page;
import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.service.*;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import com.ningct.nowcodercommunity.util.HostHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger =  LoggerFactory.getLogger(UserController.class);
    @Resource
    private UserService userService;
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${ali.endpoint}")
    private String endPoint;
    @Value("${ali.bucketName}")
    private String bucketName;
    @Value("${ali.accessKeyId}")
    private String accessKeyId;
    @Value("${ali.accessKeySecret}")
    private String accessKeySecret;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;
    @Resource
    private FollowerService followerService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private CommentService commentService;

    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    //获取上传阿里云的签证
    @RequestMapping(path = "/getPolicy",method = RequestMethod.GET)
    @ResponseBody
    public String getPolicy(){
        // host的格式为 bucketname.endpoint
        String host ="https://" +bucketName+ "." +endPoint;

        //设置前面的过期时间
        long expireTime = 600;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);

        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        try {
            //获取签证
            OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            //使用json传输数据
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("OSSAccessKeyId", accessKeyId);
            jsonObject.put("policy", encodedPolicy);
            jsonObject.put("signature", postSignature);
            jsonObject.put("host", host);
            return CommunityUtil.getJSONString(0,null,jsonObject.getInnerMap());
        }catch(Exception e){
            logger.error("签证生成失败！" +e.getMessage());
        }
        return CommunityUtil.getJSONString(1,"签证失败！");
    }

    //将头像路径更新数据库
    @RequestMapping(path = "/updateUrl",method = RequestMethod.POST)
    @ResponseBody
    public String updateUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空");
        }
        //拼接url
        String url ="https://" +bucketName+ "." +endPoint+ "/" +fileName;
        //更新到数据库
        userService.updateHeader(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);
        //标记当前是个人信息还是我的帖子，或者回复
        model.addAttribute("cur",0);
        //点赞数
        model.addAttribute("likeCount",likeService.finUserLikeCount(userId));
        //关注数
        model.addAttribute("followee",followerService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        //粉丝数
        model.addAttribute("follower",followerService.findFollowerCount(ENTITY_TYPE_USER, userId));

        //是否已关注
        if(hostHolder.getUser() != null) {
            model.addAttribute("hasFollowed", followerService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId));
        }
        return "/site/profile";
    }

    @RequestMapping(path = "/mypost/{userId}",method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);
        //标记当前是个人信息还是我的帖子，或者回复
        model.addAttribute("cur",1);
        //帖子数量
        model.addAttribute("postCount",discussPostService.findDiscussPostRows(userId));

        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/user/mypost/" +userId);

        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),1);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                //点赞数
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("posts", discussPosts);
        return "/site/my-post";
    }

    @RequestMapping(path = "/myreply/{userId}",method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);
        //标记当前是个人信息还是我的帖子，或者回复
        model.addAttribute("cur",2);
        //回复数量
        int count = commentService.findCommentCount(userId);
        model.addAttribute("replyCount", count);

        page.setRows(count);
        page.setPath("/user/myreply/" +userId);

        List<Comment> list = commentService.findCommentByUserId(userId, page.getOffset(), page.getLimit());

        List<Map<String, Object>> replys = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                //查询评论所属post
                int id = commentService.findPostId(comment.getId());
                //评论
                map.put("comment", comment);
                //帖子
                map.put("post", discussPostService.selectDiscussPostById(id));
                //点赞数
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,comment.getEntityId()));

                replys.add(map);
            }
        }
        model.addAttribute("replys", replys);
        return "/site/my-reply";
    }

    @RequestMapping(path = "/changepassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model){
         Map<String, Object> map = userService.updatePassword(hostHolder.getUser(),oldPassword,newPassword,confirmPassword);

         if(map == null || map.isEmpty()){
             model.addAttribute("msg", "密码修改成功！");
             model.addAttribute("target","/index");
             return "/site/operate-result";
         }else{
             model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
             model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
             model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
             return "/site/setting";
         }
    }

}
