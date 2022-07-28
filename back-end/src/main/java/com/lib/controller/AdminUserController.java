package com.lib.controller;

import com.lib.service.AdminUserService;
import com.lib.service.Impl.MailServiceImpl;
import com.lib.service.MailService;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
public class AdminUserController {
/**
 *
 * AdminUserController
 * 用于处理用户和管理员的共同事务如登录、注册等。
 *
 * @ClassName AdminController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/3 14:13
 * @Version 1.0
 **/

    @Autowired
    public AdminUserService adminUserService;

    @Autowired
    public MailService mailService = new MailServiceImpl();

    @PostMapping("/registry")
    public Result adminUserRegistry(@RequestBody Map<String,Object> reqBody){
        Result result = adminUserService.adminUserRegistry(reqBody);
        return result;
    }

    @GetMapping("/login/{loginName}/{password}")
    public Result adminUserLogin(@PathVariable("loginName") String loginName, @PathVariable("password") String password){
        Result result = adminUserService.adminUserLogin(loginName, password);
        return result;
    }

    @PutMapping("/changeCode")
    public Result adminUserChangeCode(@RequestBody Map<String,Object> reqBody){
        Result result = adminUserService.adminUserChangeCode(reqBody);
        return result;
    }

    @PostMapping("/sendVerCode")
    public Result sendVerCode(@RequestBody Map<String,Object> reqBody){
        String email = (String) reqBody.get("email");
        if(StringUtils.isEmpty(email))  return Result.create(ResultEnum.DATA_IS_NULL, null);
        Result result = mailService.sendMailForChangeCode(email);
        return result;
    }
}
