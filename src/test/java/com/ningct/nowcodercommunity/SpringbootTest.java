package com.ningct.nowcodercommunity;

import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
@SpringBootTest
public class SpringbootTest {
    @Resource
    private DiscussPostService discussPostService;
    private DiscussPost post;
    @BeforeClass
    public static void beforeClass(){
        System.out.println("class before load");
    }
    @AfterClass
    public static void afterClass(){
        System.out.println("class after load");
    }
    @Before
    public void before(){
        System.out.println("method before!");
        post = new DiscussPost();
        post.setTitle("hahhah");
        post.setContent("wwwwww");
        post.setCreateTime(new Date());
        post.setUserId(111);
        discussPostService.addDiscussPost(post);
    }
    @After
    public void after(){
        discussPostService.updatePostStatus(post.getId(),1);
        System.out.println("method after");
    }
    @Test
    public void test1(){
        DiscussPost mpost = discussPostService.selectDiscussPostById(post.getId());
        Assert.assertNotNull(mpost);
        Assert.assertEquals(post.getTitle(),mpost.getTitle());
        Assert.assertEquals(post.getContent(),mpost.getContent());
    }
    @Test
    public void test2() {
        int rows = discussPostService.updatePostScore(post.getId(), 2000.00);
        Assert.assertEquals(1,rows);

        DiscussPost mpost = discussPostService.selectDiscussPostById(post.getId());
        Assert.assertEquals(2000.00,mpost.getScore(),2);

    }
}
