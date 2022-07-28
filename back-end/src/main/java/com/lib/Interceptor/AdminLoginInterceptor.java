/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.lib.Interceptor;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台系统身份验证拦截器
 *
 * @author 13
 * @qq交流群 796794009
 * @email 2449207463@qq.com
 * @link https://github.com/newbee-ltd
 */
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Autowired
    public RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String requestServletPath = request.getServletPath();
        String token = request.getHeader("token");
        Map<String ,Object> map = new HashMap<>();
        PrintWriter writer = response.getWriter();
        System.out.println(requestServletPath);
        if(requestServletPath.startsWith("/admin")){
            if (null == request.getHeader("token")) {
                map.put("status", 401);
                writer.write(JSONObject.toJSON(map).toString());
                return false;
            } else if(StringUtils.isEmpty((String) redisTemplate.opsForValue().get("admin:" + token))){
                map.put("status", 401);
                writer.write(JSONObject.toJSON(map).toString());
                return false;
            } else {
                String str = (String) redisTemplate.opsForValue().get("admin:" + token);
                String[] arr = str.split("&");
                map.put("id", arr[0]);
                map.put("loginName", arr[1]);
                writer.write(JSONObject.toJSON(map).toString());
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
