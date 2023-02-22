package com.ningct.nowcodercommunity.service;

import com.ningct.nowcodercommunity.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeService {
    @Resource
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId, int entityType,int entityId, int entityUserId){
        String entityKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        String userKey = RedisKeyUtil.getUserLikeKey(entityUserId);
        Boolean ismember = redisTemplate.opsForSet().isMember(entityKey,userId);
        if(ismember){
            redisTemplate.opsForSet().remove(entityKey,userId);
            redisTemplate.opsForValue().decrement(userKey);
        }else{
            redisTemplate.opsForSet().add(entityKey,userId);
            redisTemplate.opsForValue().increment(userKey);
        }
    }

    //查询某个实体的点赞数
    public long findEntityLikeCount(int entityType, int entityId){
        String entityKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityKey);
    }

    //查询某个用户得到的赞数量
    public int finUserLikeCount(int userId){
        String userKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count =  (Integer) redisTemplate.opsForValue().get(userKey);
        return count == null ? 0 : count.intValue() < 0 ? 0 : count.intValue();
    }

    //查询某个用户对某个实体点赞的状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityKey, userId) ? 1 : 0;
    }
}
