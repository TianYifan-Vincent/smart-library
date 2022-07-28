/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.lib.config;


import com.lib.Interceptor.AdminLoginInterceptor;
import com.lib.Interceptor.ReaderLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LibraryWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    public AdminLoginInterceptor adminLoginInterceptor;

    @Autowired
    public ReaderLoginInterceptor readerLoginInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(adminLoginInterceptor)
//                .addPathPatterns("/admin/**");
//
//
//        registry.addInterceptor(readerLoginInterceptor)
//                .addPathPatterns("/lib/**");
    }

}
