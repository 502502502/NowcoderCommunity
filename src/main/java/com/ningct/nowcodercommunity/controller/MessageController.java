package com.ningct.nowcodercommunity.controller;

import com.alibaba.fastjson2.JSONObject;
import com.ningct.nowcodercommunity.entity.Message;
import com.ningct.nowcodercommunity.entity.Page;
import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Resource
    private MessageService messageService;
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();

        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");
        page.setLimit(5);
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());

        //将会话集合包装成map信息集合
        List<Map<String,Object>> conversations = new ArrayList<>();
        for (Message letter : conversationList) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversation",letter);
            map.put("letterCount",messageService.findLetterCount(letter.getConversationId()));
            map.put("unReadCound", messageService.findLetterUnReadCount(user.getId(), letter.getConversationId()));
            int targetId = user.getId() == letter.getFromId() ? letter.getToId() : letter.getFromId();
            map.put("target", userService.findUserById(targetId));

            conversations.add(map);
        }

        model.addAttribute("conversations",conversations);

        int letterUnreadCount = messageService.findLetterUnReadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
        page.setLimit(5);
        page.setPath("/letter/detail" +conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //获取私信列表
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        //包装私信列表
        List<Map<String, Object>> letters = new ArrayList<>();
        for (Message letter : letterList) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter",letter);
            map.put("fromUser",userService.findUserById(letter.getFromId()));
            letters.add((map));
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));
        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if(ids != null && !ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    public User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }

    public List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> list = new ArrayList<>();
        for (Message message : letterList) {
            if(message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0){
                list.add(message.getId());
            }
        }
        System.out.println(list.toString());
        return list;
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sentLetter(String toName, String content){
        User userTo = userService.findUserByName(toName);
        User userfrom = hostHolder.getUser();
        //检查发送对象是否存在
        if(userTo == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        //构造私信
        Message message = new Message();
        message.setToId(userTo.getId());
        message.setFromId(userfrom.getId());
        message.setCreateTime(new Date());
        message.setContent(content);
        String conversationId = null;
        if(userfrom.getId() < userTo.getId()){
            conversationId = userfrom.getId() +"_" +userTo.getId();
        }else{
            conversationId = userTo.getId() +"_" +userfrom.getId();
        }
        message.setConversationId(conversationId);
        //存入私信
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0 );
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)

    public String getNoticePage(Model model){
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.findLastNotice(user.getId(),TOPIC_COMMENT);
        if(message != null){
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            messageVO.put("count",messageService.findNoticeCount(user.getId(),TOPIC_COMMENT));
            messageVO.put("unread", messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT));
            model.addAttribute("commentNotice",messageVO);
        }


        //查询点赞类通知
        message = messageService.findLastNotice(user.getId(),TOPIC_LIKE);
        if(message != null){
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            messageVO.put("count",messageService.findNoticeCount(user.getId(),TOPIC_LIKE));
            messageVO.put("unread", messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE));
            model.addAttribute("likeNotice",messageVO);
        }


        //查询关注类通知
        message = messageService.findLastNotice(user.getId(),TOPIC_FOLLOW);
        if(message != null){
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            messageVO.put("count",messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW));
            messageVO.put("unread", messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW));
            model.addAttribute("followNotice",messageVO);
        }


        //查询未读消息数
        model.addAttribute("letterUnreadCount",messageService.findLetterUnReadCount(user.getId(),null));
        model.addAttribute("noticeUnreadCount",messageService.findNoticeUnreadCount(user.getId(),null));

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)

    public  String getNoticeDetial(@PathVariable("topic") String topic, Page page, Model model){
        User user = hostHolder.getUser();

        //分页设置
        page.setPath("/notice/detail/" +topic);
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        //获取通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(),topic,page.getOffset(),page.getLimit());
        if(noticeList != null && !noticeList.isEmpty()){
            List<Map<String, Object>> noticeVoList = new ArrayList<>();
            for (Message notice : noticeList) {
                //包装信息
                Map<String, Object> noticeVo = new HashMap<>();
                noticeVo.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);
                noticeVo.put("user",userService.findUserById((Integer) data.get("userId")));
                noticeVo.put("entityType",data.get("entityType"));
                noticeVo.put("entityId",data.get("entityId"));
                noticeVo.put("postId", data.get("postId"));
                noticeVo.put("fromUser",userService.findUserById(notice.getFromId()));
                //加入集合
                noticeVoList.add(noticeVo);
            }
            model.addAttribute("notices",noticeVoList);
        }

        //设置已读

        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

}
