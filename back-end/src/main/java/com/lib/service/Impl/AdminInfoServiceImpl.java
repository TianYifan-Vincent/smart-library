package com.lib.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.entity.User;
import com.lib.mapper.UserMapper;
import com.lib.service.AdminInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminInfoServiceImpl implements AdminInfoService {
/**
 * @ClassName AdminInfoServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/20 15:16
 * @Version 1.0
 **/
    @Autowired
    public UserMapper userMapper;

    @Autowired
    public RedisTemplate redisTemplate;

    @Override
    public Result getAdminInfo(Integer id) {
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        User user = userMapper.selectByPrimaryKey(id);
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", user);
        if (user == null) return Result.create(ResultEnum.QUERY_ERROR, null);
        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Override
    public Result updateAdminInfo(Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String oldPwd = (String) map.get("oldPwd");
        String newPwd = (String) map.get("newPwd");
        String newMd5Pwd = "";
        String gender = (String) map.get("gender");
        Integer age = (Integer) map.get("age");
        String email = (String) map.get("email");
        String introduce = (String) map.get("introduce");
        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        String newSalt = RandomUtil.randomString(6);
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        if((StringUtils.isEmpty(oldPwd)&&!StringUtils.isEmpty(newPwd)) || (!StringUtils.isEmpty(oldPwd)&&StringUtils.isEmpty(newPwd)))
            return Result.create(ResultEnum.BAD_FORMAT, null);
        if (!StringUtils.isEmpty(email)&&!email.matches(tegex))
            return Result.create(ResultEnum.WRONG_FORMAT_EMAIL, null);
        User user = userMapper.selectByPrimaryKey(id);
        String oldEmail = user.getEmail();
        if(!StringUtils.isEmpty(oldPwd)&&!StringUtils.isEmpty(newPwd)){
            String oldSalt = user.getSalt();
            String tempPwd = SecureUtil.md5(oldPwd + oldSalt);
            if(!tempPwd.equals(user.getPassword())){
                return Result.create(ResultEnum.WRONG_CODE, null);
            }
            newMd5Pwd = SecureUtil.md5(newPwd + newSalt);
        }
        int i = userMapper.updateAdminInfo(id, newSalt, newMd5Pwd, gender, age, email, introduce);
        if(i < 1) return Result.create(ResultEnum.UPDATE_ERROR,null);

        if(!StringUtils.isEmpty(email)) {
            if (!StringUtils.isEmpty(oldEmail)) {
                redisTemplate.opsForSet().remove("lib:AdminEmail", oldEmail);
            }
            redisTemplate.opsForSet().add("lib:AdminEmail", email);
        }
        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }

    @Override
    public Result getWaitingApproveCount() {
        Integer cnt = redisTemplate.opsForSet().size("lib:WaitingMessagesList").intValue();
        if(cnt == null) return Result.create(ResultEnum.QUERY_ERROR, null);
        return Result.create(ResultEnum.QUERY_SUCCESS, cnt);
    }
}
