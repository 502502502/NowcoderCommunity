package com.ningct.nowcodercommunity;

import com.ningct.nowcodercommunity.entity.*;
import com.ningct.nowcodercommunity.mapper.CommentMapper;
import com.ningct.nowcodercommunity.mapper.DiscussPostMapper;
import com.ningct.nowcodercommunity.mapper.MessageMapper;
import com.ningct.nowcodercommunity.mapper.UserMapper;
import com.ningct.nowcodercommunity.service.CommentService;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import com.ningct.nowcodercommunity.service.MessageService;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import com.ningct.nowcodercommunity.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CommunityApplicationTests {

    @Test
    void contextLoads() {
    }
    @Resource
    private UserMapper userMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private SensitiveFilter sensitiveFilter;
    @Resource
    private CommentService commentService;
    @Resource
    private MessageService messageService;

    @Test
    public void selecttMessageCountTest(){
        System.out.println(messageService.findConversationCount(111));
    }
    @Test
    public void selectMessageTest(){
        for (Message conversation : messageService.findConversations(111, 0, 5)) {
            System.out.println(conversation.toString());
        }
    }

    @Test
    public void selectLetterCountTest(){
        messageService.findLetterCount("111_112");
    }
    @Test
    public void selectLetterTest(){
        for (Message letter : messageService.findLetters("111_112", 0, 5)) {
            System.out.println(letter.toString());
        }
    }

    @Test
    public void selectLetterUnReadCountTest(){
        System.out.println(messageService.findLetterUnReadCount(111, null));
    }

    @Test
    public void insertLetterTest(){
        Message message = new Message();
        message.setContent("你好哦，一起赌博把");
        message.setCreateTime(new Date());
        message.setFromId(123);
        message.setToId(124);
        message.setConversationId("123_124");
        messageService.addMessage(message);
    }

    @Test
    public void selectPostTest(){
        System.out.println(discussPostService.selectDiscussPostById(109).toString());
    }
    @Test
    public void selectCommentTest(){
        for (Comment comment : commentService.findCommentsByEntity(1, 228, 0, 100)) {
            System.out.println(comment.toString());
        }
    }
    @Test
    public void selectCommentCountTest(){
        System.out.println(commentService.findCommentCount(1, 228));
    }
    @Test
    public void insertCommentTest(){
        Comment comment = new Comment();
        comment.setContent("一定不能开票！");
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
    }

    @Test
    public void addDiscussPostTest(){
        DiscussPost post = new DiscussPost();
        post.setTitle("测试");
        post.setContent("测试功能是否正常，赌博<script>能不能开票</script>");
        post.setUserId(101);
        post.setScore(10);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
    }

    @Test
    public void addMessageTest(){
        Message message = new Message();
        message.setFromId(-1);
        message.setCreateTime(new Date());
        message.setContent("vfdbvdbv");
        message.setConversationId("1111");
        messageService.addMessage(message);
    }

    @Test
    public void sensitiveTest(){
        String t = "哈哈哈，我要赌博！！！我要开&……￥票！！！";
        System.out.println(sensitiveFilter.filter(t));
    }

    @Test
    public void jsonTest(){
        Map<String ,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age","20");
        System.out.println(CommunityUtil.getJSONString(0, "正常", map));
    }

//    @Test
//    public void userMapperTest(){
//        messageMapper.deleteallMessage();
//    }
    @Test
    public  void discussPostTest(){
        List<DiscussPost> list = discussPostMapper.selectList(null);
        for (DiscussPost post : list) {
            System.out.println(post.toString());
        }
    }
    @Test
    public void discussPostServiceTest(){
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, 1, 10,0);
        for (DiscussPost post : list) {
            System.out.println(post.toString());
        }
    }
    @Test
    public void discussPostRowsTest(){
        int rows = discussPostService.findDiscussPostRows(101);
        System.out.println(rows);
    }

}
