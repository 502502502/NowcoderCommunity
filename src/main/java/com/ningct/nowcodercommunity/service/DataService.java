package com.ningct.nowcodercommunity.service;

import com.ningct.nowcodercommunity.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//处理日活动用户数据
@Service
public class DataService {
    @Resource
    private RedisTemplate redisTemplate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    //记录某个ip访问
    public void recordUV(String ip){
        String key = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(key,ip);
    }

    //计算一段时间内的访问数量
    public long caculateUV(Date startDate, Date endDate){
        if(startDate == null || endDate == null){
            throw new IllegalArgumentException("日期不能为空！");
        }
        List<String> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while(!calendar.getTime().after(endDate)){
            String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keys.add(uvKey);
            calendar.add(Calendar.DATE,1);
        }
        String key = RedisKeyUtil.getUVKey(dateFormat.format(startDate), dateFormat.format(endDate));
        redisTemplate.opsForHyperLogLog().union(key,keys.toArray());
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    //记录访问的用户
    public void recordDAU(int id){
        String key = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(key,id);
    }

    //计算某个时间段访问的用户数量
    public long caculateDau(Date startDate, Date endDate){
        if(startDate == null || endDate == null){
            throw new IllegalArgumentException("日期不能为空！");
        }
        List<String> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while(!calendar.getTime().after(endDate)){
            String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keys.add(dauKey);
            calendar.add(Calendar.DATE,1);
        }
        String key = RedisKeyUtil.getDAUKey(dateFormat.format(startDate), dateFormat.format(endDate));
        redisTemplate.opsForHyperLogLog().union(key,keys.toArray());
        return redisTemplate.opsForHyperLogLog().size(key);
    }
}
