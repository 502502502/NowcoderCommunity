package com.ningct.nowcodercommunity.util;

import com.ningct.nowcodercommunity.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<User>();

    //设置本地用户
    public void setUser(User user){
        users.set(user);
    }

    //获取本地用户
    public User getUser(){
        return users.get();
    }

    //清理本地用户
    public void clear(){
        users.remove();
    }
}
