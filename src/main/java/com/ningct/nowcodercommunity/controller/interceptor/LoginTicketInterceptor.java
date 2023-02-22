package com.ningct.nowcodercommunity.controller.interceptor;

import com.ningct.nowcodercommunity.entity.LoginTicket;
import com.ningct.nowcodercommunity.entity.User;
import com.ningct.nowcodercommunity.service.MessageService;
import com.ningct.nowcodercommunity.service.UserService;
import com.ningct.nowcodercommunity.util.CookieUtil;
import com.ningct.nowcodercommunity.util.HostHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;
    @Resource
    private MessageService messageService;
    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取登录凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null){
            //获取登录信息
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查登录有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //查询并存储用户
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
                //构建用户认证的结果，存入SecurityContext以便spring Security使用
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
            int letterUnread = messageService.findLetterUnReadCount(user.getId(),null);
            int noticeUnread = messageService.findNoticeUnreadCount(user.getId(),null);
            modelAndView.addObject("allUnreadCount",letterUnread +noticeUnread);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        //Security清除认证信息
        SecurityContextHolder.clearContext();
    }
}
