package com.lib.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.entity.Reader;
import com.lib.entity.approve;
import com.lib.entity.borrowReturnBooks;
import com.lib.mapper.BookMapper;
import com.lib.mapper.ReaderMapper;
import com.lib.mapper.approveMapper;
import com.lib.mapper.borrowReturnBooksMapper;
import com.lib.service.AdminReaderService;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminReaderServiceImpl implements AdminReaderService {
/**
 * @ClassName AdminReaderServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/10 13:55
 * @Version 1.0
 **/
    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @Autowired
    public approveMapper approveMapper;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public BookMapper bookMapper;

    @Override
    public Result getAllReaders(Integer pageNo, Integer pageSize, String keyWord) {
        long begin = System.currentTimeMillis();
        if(pageNo == null || pageSize == null || StringUtils.isEmpty(keyWord)){
            return Result.create(ResultEnum.NOT_NULLABLE, null);
        }
        PageHelper.startPage(pageNo, pageSize);
        List<Reader> readers = readerMapper.selectAllByKeyWord(keyWord);
        PageInfo<Reader> pageInfo = new PageInfo<>(readers,5);
        long end = System.currentTimeMillis();
        System.out.println("总共花费:"+(end-begin)+"ms");
        return Result.create(ResultEnum.QUERY_SUCCESS, pageInfo);
    }

    @Transactional
    @Override
    public Result editReader(Map<String, Object> map) {
        Integer id =(Integer) map.get("id");
        String loginName =(String) map.get("loginName");
        String reader_name =(String) map.get("readerName");
        String gender = (String) map.get("gender");
        Integer age = (Integer) map.get("age");
        String phone = (String) map.get("phone");
        if(id == null || StringUtils.isEmpty(loginName)) return Result.create(ResultEnum.DATA_IS_NULL,null);
        String tegex1="[a-zA-Z0-9]{6,15}";
        if(!loginName.matches(tegex1)) return Result.create(ResultEnum.WRONG_FORMAT_LOGIN_NAME,null);
        //更换用户名时需要进行以下操作：在缓存中检查更换过后的用户名是否重复，若重复则报错返回更新失败；非重复则更改数据库同时更改缓存中的loginName。
        List<String> strings = readerMapper.selectLoginNameById(id);

        String originName = strings.get(0);
        if(!loginName.equals(originName)){
            if(redisTemplate.opsForSet().isMember("lib:ReaderLoginName", loginName) || redisTemplate.opsForSet().isMember("lib:AdminLoginName", loginName)) return Result.create(ResultEnum.LOGIN_NAME_IS_EXISTS,null);
            redisTemplate.opsForSet().remove("lib:ReaderLoginName", originName);
            redisTemplate.opsForSet().add("lib:ReaderLoginName", loginName);
        }

        int update = readerMapper.editReader(id, loginName, reader_name, gender, age, phone);
        if(update < 1) return Result.create(ResultEnum.UPDATE_ERROR, null);


        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result addReader(Map<String, Object> map) {
        String email = (String) map.get("email");
        String loginName =(String) map.get("loginName");
        String password = (String) map.get("password");
        String reader_name =(String) map.get("readerName");
        String gender = (String) map.get("gender");
        Integer age = (Integer) map.get("age");
        String phone = (String) map.get("phone");
        if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(password) || StringUtils.isEmpty(password)) return Result.create(ResultEnum.DATA_IS_NULL,null);


        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        if(!email.matches(tegex)) return Result.create(ResultEnum.WRONG_FORMAT_EMAIL,null);
        String tegex1="[a-zA-Z0-9]{6,15}";
        if(!loginName.matches(tegex1)) return Result.create(ResultEnum.WRONG_FORMAT_LOGIN_NAME,null);

        //判重email&loginName，重复返回
        if(redisTemplate.opsForSet().isMember("lib:ReaderEmail", email) || redisTemplate.opsForSet().isMember("lib:AdminEmail", email))
            return Result.create(ResultEnum.EMAIL_IS_EXISTS,null);
        if(redisTemplate.opsForSet().isMember("lib:ReaderLoginName", loginName) || redisTemplate.opsForSet().isMember("lib:AdminLoginName", loginName))
            return Result.create(ResultEnum.LOGIN_NAME_IS_EXISTS,null);



        // 密码来波加密
        String salt = RandomUtil.randomString(6);
        String md5pwd = SecureUtil.md5(password + salt);

        Reader reader = new Reader();
        reader.setEmail(email);
        reader.setLoginName(loginName);
        reader.setReaderName(reader_name);
        reader.setSalt(salt);
        reader.setPassword(md5pwd);
        reader.setGender(gender);
        reader.setAge(age);
        reader.setPhone(phone);

        //不重复的话，就首先把数据存在数据库中后在同步更新到缓存中
        int insert = readerMapper.insert(reader);
        if(insert < 1) return Result.create(ResultEnum.INSERT_ERROR,null);

        redisTemplate.opsForSet().add("lib:ReaderEmail",email);
        redisTemplate.opsForSet().add("lib:ReaderLoginName",loginName);

        //新增用户统计
        Long date = new Date().getTime();
        redisTemplate.opsForZSet().add("lib:stNewReader", loginName, date);


        return Result.create(ResultEnum.INSERT_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result deleteReader(Integer id) {
        Reader reader = readerMapper.selectByPrimaryKey(id);
        if(reader == null) return Result.create(ResultEnum.USER_NOT_EXIST,null);
        String loginName = reader.getLoginName();
        String email = reader.getEmail();
        if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(email)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        //删除读者前，应判断该读者是否有未完成的借阅记录
        List<borrowReturnBooks> borrowReturnBooks = borrowReturnBooksMapper.selectUnfinishedByReaderId(id);
        if (borrowReturnBooks.size() > 0){
            return Result.create(ResultEnum.READER_NOT_FINISH,null);
        }

        List<approve> list = approveMapper.selectByReaderId(id);

        //如该读者没有未完成的借阅记录，将该读者从数据库删除后再删除缓存相关信息。
        int i = readerMapper.deleteByPrimaryKey(id);
        if(i < 1) return Result.create(ResultEnum.DELETE_ERROR,null);



        //对缓存进行对应操作
        redisTemplate.opsForSet().remove("lib:ReaderEmail", email);
        redisTemplate.opsForSet().remove("lib:ReaderLoginName", loginName);
        redisTemplate.opsForZSet().remove("lib:ReaderVioCount", id);
        for(approve t : list){
            String name = readerMapper.selectLoginNameById(t.getReaderId()).get(0);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getDate());
            if(t.getStatus() == 0){
                Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" + name + "*" + date + "*").build());
                redisTemplate.opsForSet().remove("lib:WaitingMessagesList", scan.next().toString());
            } else{
                Cursor scan = redisTemplate.opsForSet().scan("lib:FinishedMessagesList", ScanOptions.scanOptions().match("*" + name + "*" + date + "*").build());
                redisTemplate.opsForSet().remove("lib:FinishedMessagesList", scan.next().toString());
            }
        }
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result deleteReaders(Map<String, Object> map) {
        List<Integer> ids = (List<Integer>) map.get("ids");
        if(ids == null || ids.size() == 0) return Result.create(ResultEnum.DATA_IS_NULL,null);
        for(int id : ids){
            Reader reader = readerMapper.selectByPrimaryKey(id);
            if(reader == null) continue;
            String loginName = reader.getLoginName();
            String email = reader.getEmail();
            if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(email)) return Result.create(ResultEnum.DATA_IS_NULL,null);

            //删除读者前，应判断该读者是否有未完成的借阅记录
            List<borrowReturnBooks> borrowReturnBooks = borrowReturnBooksMapper.selectUnfinishedByReaderId(id);
            if (borrowReturnBooks.size() > 0){
                return Result.create(ResultEnum.READER_NOT_FINISH,null);
            }

            List<approve> list = approveMapper.selectByReaderId(id);

            //如该读者没有未完成的借阅记录，将该读者从数据库删除后再删除缓存相关信息。
            int i = readerMapper.deleteByPrimaryKey(id);
            if(i < 1) return Result.create(ResultEnum.DELETE_ERROR,null);

            //对缓存进行对应操作
            redisTemplate.opsForSet().remove("lib:ReaderEmail", email);
            redisTemplate.opsForSet().remove("lib:ReaderLoginName", loginName);
            redisTemplate.opsForZSet().remove("lib:ReaderVioCount", id);
            redisTemplate.opsForValue().decrement("lib:WaitingMessages", (long) list.size());
            for(approve t : list){
                String name = readerMapper.selectLoginNameById(t.getReaderId()).get(0);
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getDate());
                if(t.getStatus() == 0){
                    Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" + name + "*" + date + "*").build());
                    redisTemplate.opsForSet().remove("lib:WaitingMessagesList", scan.next().toString());
                } else{
                    Cursor scan = redisTemplate.opsForSet().scan("lib:FinishedMessagesList", ScanOptions.scanOptions().match("*" + name + "*" + date + "*").build());
                    redisTemplate.opsForSet().remove("lib:FinishedMessagesList", scan.next().toString());
                }
            }
        }
        return Result.create(ResultEnum.DELETE_SUCCESS,null);
    }


}
