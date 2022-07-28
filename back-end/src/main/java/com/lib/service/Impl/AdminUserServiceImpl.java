package com.lib.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.lib.entity.Reader;
import com.lib.entity.User;
import com.lib.mapper.ReaderMapper;
import com.lib.mapper.UserMapper;
import com.lib.service.AdminUserService;
import com.lib.service.MailService;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

@Service
public class AdminUserServiceImpl implements AdminUserService {
/**
 * @ClassName AdminUserServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/3 15:37
 * @Version 1.0
 **/

    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public UserMapper userMapper;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public MailService mailService;

    @Override
    public Result adminUserRegistry(Map<String, Object> map) {
        Reader reader = new Reader();

        //注册获取：邮箱，账号，密码
        //注册只针对一般用户
        String email = (String) map.get("email");
        String loginName = (String) map.get("loginName");
        String password = (String) map.get("password");
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        /*
           格式判断：
           邮箱:xxx@xxx.com/cn
           账号:长度大于6位小于12位，格式为字母或数字
        **/
        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        if(!email.matches(tegex)) return Result.create(ResultEnum.WRONG_FORMAT_EMAIL,null);
        String tegex1="[a-zA-Z0-9]{6,15}";
        if(!loginName.matches(tegex1)) return Result.create(ResultEnum.WRONG_FORMAT_LOGIN_NAME,null);


        //业务代码：添加新的用户信息。
        //重复判断，保证邮箱或账号不被注册过: 到redis库中查询对应的邮箱或者账号是否存在
        if (redisTemplate.opsForSet().isMember("lib:ReaderEmail", email)){
            return Result.create(ResultEnum.EMAIL_IS_EXISTS,null);
        }
        if (redisTemplate.opsForSet().isMember("lib:ReaderLoginName", loginName)){
            return Result.create(ResultEnum.LOGIN_NAME_IS_EXISTS,null);
        }

        //密码加密处理：MD5+salt(6位)
        String salt = RandomUtil.randomString(6);
        String md5Pwd = SecureUtil.md5(password + salt);


//        String path = "src/main/resources/static/image/未知头像.jpg";
//        File file = new File(path);
//        byte[] image = new byte[0];
//        try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            ByteBuffer nbf = ByteBuffer.allocate((int) file.length());
//            byte[] buffer = new byte[1024];
//            int len = 0,offset = 0;
//            while((len = fileInputStream.read(buffer)) != -1){
//                if(len != 1024) nbf.put(buffer,0,len);
//                else nbf.put(buffer);
//                offset += len;
//            }
//            fileInputStream.close();
//            image = nbf.array();
//            if(image.length == 0) return Result.create(ResultEnum.ERROR_TRANSFER, null);
//
//        } catch (FileNotFoundException e) {
//            System.out.println("文件路径出错");
//            e.printStackTrace();
//            return Result.create(ResultEnum.WRONG_FILEPATH, null);
//        } catch (IOException e) {
//            System.out.println("数据转换出错");
//            e.printStackTrace();
//            return Result.create(ResultEnum.ERROR_TRANSFER, null);
//        }

        //不重复时，将对应数据插入数据库后，对缓存进行更新。
        reader.setEmail(email);
        reader.setLoginName(loginName);
        reader.setPassword(md5Pwd);
        reader.setSalt(salt);
        reader.setIsvalid(1);
        reader.setCreditmoney(new BigDecimal(0));
        reader.setDeposit(new BigDecimal(0));




        int insert = readerMapper.insert(reader);
        if(insert <= 0) return Result.create(ResultEnum.INSERT_ERROR, null);

        //缓存中放入email && loginName
        redisTemplate.opsForSet().add("lib:ReaderEmail", email);
        redisTemplate.opsForSet().add("lib:ReaderLoginName", loginName);

        //新增用户统计
        Long date = new Date().getTime();
        redisTemplate.opsForZSet().add("lib:stNewReader", loginName, date);

        return Result.create(ResultEnum.INSERT_SUCCESS,null);
    }

    @Override
    public Result adminUserLogin(String loginName,String password) {
        List<User> users = new ArrayList<>();
        List<Reader> readers = new ArrayList<>();
        String status = new String();
        String token = UUID.randomUUID()+"";

        //登录分读者登录和管理员登录
        String loginStr = loginName;
        if(StringUtils.isEmpty(loginStr) || StringUtils.isEmpty(password)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        //登录模块：先判断是邮箱登录还是账号登录，去缓存中查找是否存在，存在进数据库比对密码
        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        String tegex1="[a-zA-Z0-9]{6,15}";
        if(!loginStr.matches(tegex) && !loginStr.matches(tegex1)) return Result.create(ResultEnum.WRONG_FORMAT_LOGIN_NAME,null);

        boolean readerE = redisTemplate.opsForSet().isMember("lib:ReaderEmail", loginStr);
        boolean userE = redisTemplate.opsForSet().isMember("lib:AdminEmail", loginStr);
        boolean readerL = redisTemplate.opsForSet().isMember("lib:ReaderLoginName", loginStr);
        boolean userL = redisTemplate.opsForSet().isMember("lib:AdminLoginName", loginStr);
        //邮箱登录
        if(loginStr.matches(tegex)){
            if(!readerE && !userE) return Result.create(ResultEnum.USER_NOT_EXIST, null);
            if(readerE){
                status = "reader";
                readers = readerMapper.selectReaderByEmail(loginStr);
                if (readers.size() < 1) return Result.create(ResultEnum.DATA_CACHE_DIF, null);
            }
            if(userE) {
                status = "admin";
                users = userMapper.selectUserByEmail(loginStr);
                if (users.size() < 1) return Result.create(ResultEnum.DATA_CACHE_DIF, null);
            }
        }
        //账号登录
        if(loginStr.matches(tegex1)){
            if(!readerL && !userL) return Result.create(ResultEnum.USER_NOT_EXIST, null);
            if(readerL){
                status = "reader";
                readers = readerMapper.selectReaderByLoginName(loginStr);
                if (readers.size() < 1) return Result.create(ResultEnum.DATA_CACHE_DIF, null);
            }
            if(userL) {
                status = "admin";
                users = userMapper.selectUserByLoginName(loginStr);
                if (users.size() < 1) return Result.create(ResultEnum.DATA_CACHE_DIF, null);
            }
        }

        if(status.equals("admin")){
            User user = users.get(0);
            String salt = user.getSalt();
            String md5pwd = SecureUtil.md5(password + salt);
            if(!md5pwd.equals(user.getPassword())) return Result.create(ResultEnum.WRONG_CODE, null);
            redisTemplate.opsForValue().set("admin:" + token, user.getId() + "&" + user.getLoginName(), Duration.ofMinutes(1));
            Map<String,Object> map = new HashMap<>();
            map.put("token", token);
            map.put("user", user);
            return Result.create(ResultEnum.ADMIN_LOGIN_SUCCESS, map);
        }
        else {
            Reader reader = readers.get(0);
            String salt = reader.getSalt();
            String md5pwd = SecureUtil.md5(password + salt);
            if(!md5pwd.equals(reader.getPassword())) return Result.create(ResultEnum.WRONG_CODE, null);
            redisTemplate.opsForValue().set("reader:" + token, reader.getId() + "&" + reader.getLoginName(), Duration.ofMinutes(1));
            Map<String,Object> map = new HashMap<>();
            map.put("token", token);
            map.put("user", reader);
            return Result.create(ResultEnum.READER_LOGIN_SUCCESS, map);
        }
    }

    @Override
    public Result adminUserChangeCode(Map<String, Object> map){
        String email = (String) map.get("email");
        String password = (String) map.get("password");
        String confirmCode = (String) map.get("confirmCode");

        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(confirmCode) || StringUtils.isEmpty(password)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        if(!email.matches(tegex)) return Result.create(ResultEnum.WRONG_FORMAT_EMAIL,null);

        //验证用户输入的验证码是否正确
        Object o = redisTemplate.opsForValue().get("Verify:" + email);
        if(o == null) return Result.create(ResultEnum.VERIFY_CODE_NOT_FOUND,null);
        String Code = o.toString().substring(0,6);
        String status = o.toString().substring(6);
        if(o == null || !Code.equals(confirmCode)) return Result.create(ResultEnum.WRONG_VERIFY_CODE,null);
        redisTemplate.delete("Verify:" + email);

        //更新该用户的盐和新密码
        String salt = RandomUtil.randomString(6);
        String md5pwd = SecureUtil.md5(password + salt);
        System.out.println(status);
        int insert = 0;
        if(status.equals("User")) insert = userMapper.updatePasswordAndSaltByEmail(email, salt, md5pwd);
        if(status.equals("Reader")) insert = readerMapper.updatePasswordAndSaltByEmail(email, salt, md5pwd);
        if(insert < 1) return Result.create(ResultEnum.UPDATE_ERROR,null);
        return Result.create(ResultEnum.UPDATE_SUCCESS,null);
    }
}
