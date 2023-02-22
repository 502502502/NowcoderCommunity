package com.ningct.nowcodercommunity.event;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectResult;
import com.ningct.nowcodercommunity.entity.DiscussPost;
import com.ningct.nowcodercommunity.entity.Event;
import com.ningct.nowcodercommunity.entity.Message;
import com.ningct.nowcodercommunity.service.DiscussPostService;
import com.ningct.nowcodercommunity.service.ElasticSearchService;
import com.ningct.nowcodercommunity.service.MessageService;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {

    private static  final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private ElasticSearchService elasticSearchService;
    @Value("${wk.image.command}")
    private String getWkImageCmd;
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${ali.accessKeyId}")
    private String accessKeyId;
    @Value("${ali.accessKeySecret}")
    private String accessKeySecret;
    @Value("${ali.bucketName}")
    private String bucketName;
    @Value("${ali.endpoint}")
    private String endPoint;
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    //关注，点赞，评论事件
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }

    //帖子发布事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePostOnES(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("更新帖子事件为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("更新帖子事件格式错误!");
            return;
        }

        // 更新ES帖子
        DiscussPost post = discussPostService.selectDiscussPostById(event.getEntityId());
        elasticSearchService.addPost(post);

    }

    //帖子删除事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeletePostOnES(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("更新帖子事件为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("更新帖子事件格式错误!");
            return;
        }

        // 删除ES帖子
        elasticSearchService.deletePost(event.getEntityId());

    }

    //生成长图，存储本地，上传阿里云
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShare(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("生成长图事件为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("事件格式错误!");
            return;
        }
        Map<String, Object> data = event.getData();
        String htmlUrl = (String) data.get("htmlUrl");
        String fileName = (String) data.get("fileName");
        String suffix = (String) data.get("suffix");

        String cmd = getWkImageCmd + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            logger.error("生成长图失败！" +e.getMessage());
        }

        //开启定时器，检查是否生成图片，上传到阿里云
        UpdateTask updateTask = new UpdateTask(fileName,suffix);
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(updateTask, 500);
        updateTask.setFuture(future);
    }

    //定时任务，将图片上传云服务器
    class UpdateTask implements Runnable{
        private String fileName;
        private String suffix;
        //任务返回信息
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
        private int updateTimes;
        public void setFuture(Future future){
            this.future = future;
        }
        public UpdateTask(String fileName, String suffix){
            this.fileName = fileName;
            this.suffix = suffix;
            startTime =System.currentTimeMillis();
        }

        @Override
        public void run() {
            //生成失败
            if(System.currentTimeMillis() -startTime >30000){
                logger.error("执行时间过长，终止任务！");
                future.cancel(true);
                return;
            }
            //上传失败
            if(updateTimes >= 3){
                logger.error("上传次数过多，终止任务！");
                future.cancel(true);
                return;
            }

            //拼接文件路径
            String path = wkImageStorage+"/"+fileName+suffix;
            //查看文件是否存在
            File file = new File(path);

            //长图已经生成，上传
            if(file.exists()){
                logger.info(String.format("开始第%d次上传%s", ++updateTimes,fileName));
                //上传
                OSS ossClient = new OSSClientBuilder().build(endPoint,accessKeyId,accessKeySecret);
                try {
                    PutObjectResult result = ossClient.putObject(bucketName, fileName+suffix, file);
                    //上传成功
                    logger.info(String.format("第%d次上传成功【%s】", updateTimes,fileName));
                    future.cancel(true);
                }catch (OSSException oe){
                    //上传失败
                    logger.error(String.format("开始第%d次上传失败【%s】", updateTimes,fileName) + oe.getErrorCode());
                }
            }else{
                //等待图片上传
                logger.info("等待图片生成+【"+fileName+"】.");
            }
        }
    }
}
