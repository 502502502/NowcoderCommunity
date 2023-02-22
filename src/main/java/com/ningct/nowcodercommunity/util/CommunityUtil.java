package com.ningct.nowcodercommunity.util;

import com.alibaba.fastjson.JSONObject;

import com.ningct.nowcodercommunity.entity.DiscussPost;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //随机生成字符串
    public  static  String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    //MD5加密
    public  static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes(StandardCharsets.UTF_8));
    }

    //将json格式字符串转成帖子对象
    public static DiscussPost getPostFromMap(String json){
        return JSONObject.parseObject(json,DiscussPost.class);
    }

    //将帖子转成json格式字符串
    public static String getJsonFromPost(DiscussPost post){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",post.getId());
        jsonObject.put("userId",post.getUserId());
        jsonObject.put("title",post.getTitle());
        jsonObject.put("content",post.getContent());
        jsonObject.put("commentCount",post.getCommentCount());
        jsonObject.put("score",post.getScore());
        jsonObject.put("status",post.getStatus());
        jsonObject.put("type",post.getType());
        Date date = post.getCreateTime();
        if(date != null) {
            jsonObject.put("createTime", format.format(date));
        }
        return jsonObject.toJSONString();
    }

    //获取json格式的返回信息
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",String.valueOf(code));
        json.put("msg",msg);
        if(map != null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg, null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null, null);
    }
}
