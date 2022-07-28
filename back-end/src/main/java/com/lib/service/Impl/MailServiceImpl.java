package com.lib.service.Impl;

import cn.hutool.core.util.RandomUtil;
import com.lib.entity.Reader;
import com.lib.entity.User;
import com.lib.service.MailService;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MailServiceImpl implements MailService {
/**
 * @ClassName MailServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/4 15:17
 * @Version 1.0
 **/
    @Value("${spring.mail.username}")
    private String mailUsername;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Result sendMailForChangeCode(String email) {
        // 发送之前确保邮箱是注册过的，否则会出错
        boolean readerE = redisTemplate.opsForSet().isMember("lib:ReaderEmail", email);
        boolean userE = redisTemplate.opsForSet().isMember("lib:AdminEmail", email);
        String status = new String();
        if(!readerE && !userE) return Result.create(ResultEnum.EMAIL_NOT_EXISTS,null);
        if(readerE) status = "Reader";
        if(userE) status = "User";

        //邮箱登录

        // 创建邮件对象
        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        if(!email.matches(tegex)) return Result.create(ResultEnum.WRONG_FORMAT_EMAIL,null);
        String confirmCode = RandomUtil.randomNumbers(6);
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMailMessage,true);
            // 设置邮件主题
            message.setSubject("欢迎使用智慧图书馆 - 个人密码找回");
            message.setFrom(mailUsername);
            // 设置邮件接收者
            message.setTo(email);
            // 设置邮件抄送人
//            message.setCc();
            message.setSentDate(new Date());
            //设置上下文环境
            Context context = new Context();
            context.setVariable("confirmCode", confirmCode);
            String text = templateEngine.process("changeCode.html",context);
            //邮件发送
            message.setText(text,true);
        } catch (MessagingException e) {
            e.printStackTrace();
            return Result.create(ResultEnum.EMAIL_SEND_ERROR,null);
        }
        confirmCode = confirmCode.concat(status);
        javaMailSender.send(mimeMailMessage);
        redisTemplate.opsForValue().set("Verify:"+email, confirmCode, 3, TimeUnit.MINUTES);
        return Result.create(ResultEnum.EMAIL_SEND_SUCCESS,null);
    }
}
