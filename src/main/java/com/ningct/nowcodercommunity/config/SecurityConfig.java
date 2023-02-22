package com.ningct.nowcodercommunity.config;
import com.ningct.nowcodercommunity.util.CommunityConstant;
import com.ningct.nowcodercommunity.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/resource/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //页面对角色授权
        http.authorizeRequests()
                .mvcMatchers(
                        "/user/setting",
                        "/user/update",
                        "/discuss/add",
                        "/comment/add",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "follow",
                        "unfollow")
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR)
                .mvcMatchers(
                        "/discuss/top",
                        "/discuss/wonderful")
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR)
                .mvcMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**")
                .hasAnyAuthority(
                        AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够的处理
        http.exceptionHandling()
                //未登录
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String s = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(s)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter printWriter = response.getWriter();
                            printWriter.write(CommunityUtil.getJSONString(403, "您还未登录！"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String s = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(s)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter printWriter = response.getWriter();
                            printWriter.write(CommunityUtil.getJSONString(403, "您没有访问此功能的权限！"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        //覆盖底层退出登录逻辑
        http.logout().logoutUrl("/securitylogut");
    }
}
