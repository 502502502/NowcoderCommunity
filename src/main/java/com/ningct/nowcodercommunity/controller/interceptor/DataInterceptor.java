package com.ningct.nowcodercommunity.controller.interceptor;

import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.service.DataService;
import com.ningct.nowcodercommunity.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;
    @Resource
    private DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //记录UV
        dataService.recordUV(request.getRemoteHost());
        //记录DAU
        User holderUser = hostHolder.getUser();
        if(holderUser != null){
            dataService.recordDAU(holderUser.getId());
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
