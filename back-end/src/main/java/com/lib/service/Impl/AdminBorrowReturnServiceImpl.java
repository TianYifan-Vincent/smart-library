package com.lib.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.controller.vo.ApproveModel;
import com.lib.controller.vo.brbEntity;
import com.lib.entity.approve;
import com.lib.entity.borrowReturnBooks;
import com.lib.mapper.BookMapper;
import com.lib.mapper.ReaderMapper;
import com.lib.mapper.approveMapper;
import com.lib.mapper.borrowReturnBooksMapper;
import com.lib.service.AdminBorrowReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.util.DateUtils;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service
public class AdminBorrowReturnServiceImpl implements AdminBorrowReturnService {
/**
 * @ClassName AdminBorrowReturnServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/17 15:12
 * @Version 1.0
 **/
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public approveMapper approveMapper;

    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @Override
    public Result getAllWaitingApproves(Integer pageNo, Integer pageSize) {
        if(pageNo == null || pageSize == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        List<String> members = new ArrayList<>();
        List<ApproveModel> membersList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Integer WCount = 0;
        Integer FCount = 0;
        //需要返回：未读消息和已读消息的条数，以及所有未读消息
        PageHelper.startPage(pageNo, pageSize);
        List<approve> list = approveMapper.selectAllWaitingMessages();
        PageInfo pageInfo = new PageInfo(list, 5);

        for(Object obj : pageInfo.getList()){
            approve approve = (approve) obj;
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(approve.getDate());
            Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" + time + "*" + approve.getBookId() + "*").build());
            if(scan.hasNext()) {
                String str = scan.next().toString();
                System.out.println(str);
                members.add(str);
            }
        }


        WCount = redisTemplate.opsForSet().size("lib:WaitingMessagesList").intValue();
        FCount = redisTemplate.opsForSet().size("lib:FinishedMessagesList").intValue();
        if(members == null || WCount == null || FCount == null) return Result.create(ResultEnum.QUERY_ERROR, null);

        for(String string : members) {
            String[] contain = string.split("&");
            String content = contain[0];
            String Date = contain[1];
            ApproveModel approveModel = new ApproveModel(content,Date);
            membersList.add(approveModel);
        }

        pageInfo.setList(membersList);


        map.put("list", pageInfo);
        map.put("FinishedCount", FCount);
        map.put("WaitingCount", WCount);

        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Transactional
    @Override
    public Result allowWaitingApproves(Map<String, Object> map) {
        //获取审批内容、日期及管理员id
        String content = (String) map.get("content");
        String Date = (String) map.get("date");
        Integer userId = (Integer) map.get("userId");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
        } catch (ParseException e) {
            System.out.println("my bad:parse");
            return Result.create(ResultEnum.UPDATE_ERROR, null);
        }

        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(date) || userId == null) return Result.create(ResultEnum.DATA_IS_NULL,null);

        Integer operation = null;
        Integer readerId = null;
        Integer rEndIndex = null;

        //预处理提取信息
        if(content.contains("借阅")){
            operation = 0;
            rEndIndex = content.indexOf("借");
        } else{
            operation = 1;
            rEndIndex = content.indexOf("归");
        }

        String loginName = content.substring(0, rEndIndex);
        readerId = readerMapper.selectReaderByLoginName(loginName).get(0).getId();



        //从缓存中获取到对应信息，从中获取到bookId和借阅的月数
        Integer bookId = null;
        Integer borrowTime = null;
        Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" + loginName + "*" + Date + "*").build());
        System.out.println(content + "&" + Date);
        String str = new String();
        try {
            str = scan.next().toString();
        } catch (NoSuchElementException e){
            System.out.println("my bad:nosuchelement");
            return Result.create(ResultEnum.UPDATE_ERROR,null);
        }

        int t = str.lastIndexOf("&") + 1;
        int j = str.lastIndexOf("&", t-2);
        bookId = Integer.parseInt(str.substring(t));
        borrowTime = Integer.parseInt(str.substring(j + 1, t - 1));
        if(bookId == null || borrowTime == null) return Result.create(ResultEnum.DATA_IS_NULL, null);


        //计算理想归还时间
        int days = borrowTime * 30;
        Calendar currentdate = Calendar.getInstance();
        currentdate.add(Calendar.DATE, days);
        Date planTime = currentdate.getTime();


        if(operation == 0 && borrowReturnBooksMapper.selectCountByReaderId(readerId) >= 5){
            System.out.println(bookId);
            System.out.println(readerId);
            System.out.println(operation);
            System.out.println(date);
            int i = approveMapper.deleteWaitingByParam(bookId, readerId, operation, date, 0);
            String searchContent = "*" + loginName + "*" + Date + "*";
            Cursor scan1 = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match(searchContent).build());
            String str1 = new String();
            str1 = scan1.next().toString();
            redisTemplate.opsForSet().remove("lib:WaitingMessagesList", str1);
            return Result.create(ResultEnum.BEYOND_MAX_BORROW, null);
        }

        //修改数据库approve表的同时，在brb表中添加一条相应的记录，同时不要忘记修改缓存
        int i = approveMapper.updateStatusByBookIdAndReaderIdAndDateAndOperation(1, bookId, readerId, operation, date);
        if(i < 1) {
            System.out.println("my bad:i<1");
            return Result.create(ResultEnum.UPDATE_ERROR, null);
        }

        borrowReturnBooks borrowReturnBooks = new borrowReturnBooks();
        borrowReturnBooks.setBookId(bookId);
        borrowReturnBooks.setBorrowTime(new Date());
        borrowReturnBooks.setReaderId(readerId);
        borrowReturnBooks.setUserId(userId);
        borrowReturnBooks.setStatus(1);
        borrowReturnBooks.setPlanTime(planTime);

        Integer category = bookMapper.selectCategoryByPrimaryKey(bookId).get(0);
        //对brb表的修改应该分类讨论---借阅：brb表加入新记录 归还：brb表修改状态
        if(operation == 0) {
            //借阅情况下，在brb表插入相应一条记录

            int insert = borrowReturnBooksMapper.insert(borrowReturnBooks);
            if (insert < 1) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    System.out.println("my bad:insert < 1");
                    return Result.create(ResultEnum.UPDATE_ERROR, null);
                }
            }

            if(!redisTemplate.opsForZSet().addIfAbsent("lib:stBookBorrowedCount", bookId, 1)){
                redisTemplate.opsForZSet().incrementScore("lib:stBookBorrowedCount", bookId, 1);
            }
            redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, -1);
            bookMapper.decrCopyCountById(bookId);

            int year = calendar.get(Calendar.YEAR);
            int week = calendar.get(Calendar.WEEK_OF_YEAR);
            int month = calendar.get(Calendar.MONTH);
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            String str1 = "lib:" + year + "." + week;
            String str2 = "lib:" + year + "&" + month;
            String key1 = String.valueOf(weekday);
            String key2 = String.valueOf(category);
            if(!redisTemplate.opsForHash().putIfAbsent(str1, key1 , 1)){
                redisTemplate.opsForHash().increment(str1, key1, 1);
            }

            if(!redisTemplate.opsForHash().putIfAbsent(str2, key2, 1)){
                redisTemplate.opsForHash().increment(str2, key2, 1);
            }


        } else {
            //归还情况下在更新brb记录信息时需判断当前用户是否逾期归还
            Date planTime1 = borrowReturnBooksMapper.getPlanTimeByReaderIdAndBookId(readerId, bookId);
            if(planTime1.before(new Date())){
                //逾期归还,修改状态，缓存
                int i1 = borrowReturnBooksMapper.updateStatusAndReturnTime(3, new Date(), readerId, bookId);
                if (i1 < 1) {
                    return Result.create(ResultEnum.UPDATE_ERROR, null);
                }
                if(!redisTemplate.opsForZSet().addIfAbsent("lib:ViolateRank", readerId, 1)){
                    redisTemplate.opsForZSet().incrementScore("lib:ViolateRank", readerId, 1);
                }
            } else{
                //期限内归还
                int i2 = borrowReturnBooksMapper.updateStatusAndReturnTime(2, new Date(), readerId, bookId);
                if (i2 < 1) {
                    return Result.create(ResultEnum.UPDATE_ERROR, null);
                }
            }
            bookMapper.incrCopyCountById(bookId);
            redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, 1);
        }


        //处理缓存
        redisTemplate.opsForSet().remove("lib:WaitingMessagesList", str);
        redisTemplate.opsForSet().add("lib:FinishedMessagesList", str);
        return Result.create(ResultEnum.UPDATE_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result allAllowWaitingApproves(Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        List<ApproveModel> list = new ArrayList<>();
        Set<String> members = redisTemplate.opsForSet().members("lib:WaitingMessagesList");
        if(members == null || userId == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        for(String member : members){
            String[] split = member.split("&");
            String content = split[0];
            String date = split[1];
            if(StringUtils.isEmpty(content) || StringUtils.isEmpty(date))  return Result.create(ResultEnum.DATA_IS_NULL, null);
            ApproveModel approveModel = new ApproveModel();
            approveModel.setContent(content);
            approveModel.setDate(date);
            list.add(approveModel);
        }
        for(ApproveModel approveModel : list){
            Calendar calendar = Calendar.getInstance();
            //获取审批内容、日期及管理员id
            String content = approveModel.getContent();
            String Date = approveModel.getDate();
            Date date = calendar.getTime();
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
            } catch (ParseException e) {
                return Result.create(ResultEnum.UPDATE_ERROR, null);
            }
            try {
                if (StringUtils.isEmpty(content) || StringUtils.isEmpty(date)) {
                    throw new Exception();
                }
            } catch (Exception e){
                return Result.create(ResultEnum.DATA_IS_NULL,null);
            }

            Integer operation = null;
            Integer readerId = null;
            Integer rEndIndex = null;

            //预处理提取信息
            if(content.contains("借阅")){
                operation = 0;
                rEndIndex = content.indexOf("借");
            } else{
                operation = 1;
                rEndIndex = content.indexOf("归");
            }

            String loginName = content.substring(0, rEndIndex);
            readerId = readerMapper.selectReaderByLoginName(loginName).get(0).getId();


            //从缓存中获取到对应信息，从中获取到bookId和借阅的月数
            Integer bookId = null;
            Integer borrowTime = null;
            Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" +  loginName + "*" + Date + "*").build());

            String str = new String();
            try {
                str = scan.next().toString();
            } catch (NoSuchElementException e){
                return Result.create(ResultEnum.UPDATE_ERROR,null);
            }
            int t = str.lastIndexOf("&") + 1;
            int j = str.lastIndexOf("&", t-2);
            bookId = Integer.parseInt(str.substring(t));
            borrowTime = Integer.parseInt(str.substring(j + 1, t - 1));

            try {
                if(bookId == null || borrowTime == null) {
                    throw new Exception();
                }
            } catch (Exception e){
                return Result.create(ResultEnum.DATA_IS_NULL,null);
            }


            //计算理想归还时间
            int days = borrowTime * 30;
            Calendar currentdate = Calendar.getInstance();
            currentdate.add(Calendar.DATE, days);
            Date planTime = currentdate.getTime();


            if(operation == 0 && borrowReturnBooksMapper.selectCountByReaderId(readerId) >= 5){
                System.out.println(bookId);
                System.out.println(readerId);
                System.out.println(operation);
                System.out.println(date);
                int i = approveMapper.deleteWaitingByParam(bookId, readerId, operation, date, 0);
                String searchContent = "*" + loginName + "*" + Date + "*";
                Cursor scan1 = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match(searchContent).build());
                String str1 = new String();
                str1 = scan1.next().toString();
                redisTemplate.opsForSet().remove("lib:WaitingMessagesList", str1);
                return Result.create(ResultEnum.BEYOND_MAX_BORROW, null);
            }



            //修改数据库approve表的同时，在brb表中添加一条相应的记录，同时不要忘记修改缓存
            int i = approveMapper.updateStatusByBookIdAndReaderIdAndDateAndOperation(1, bookId, readerId, operation, date);
            if(i < 1) return Result.create(ResultEnum.UPDATE_ERROR, null);

            borrowReturnBooks borrowReturnBooks = new borrowReturnBooks();
            borrowReturnBooks.setBookId(bookId);
            borrowReturnBooks.setBorrowTime(new Date());
            borrowReturnBooks.setReaderId(readerId);
            borrowReturnBooks.setUserId(userId);
            borrowReturnBooks.setStatus(1);
            borrowReturnBooks.setPlanTime(planTime);


            Integer category = bookMapper.selectCategoryByPrimaryKey(bookId).get(0);
            //对brb表的修改应该分类讨论---借阅：brb表加入新记录 归还：brb表修改状态
            if(operation == 0) {
                //借阅情况下，在brb表插入相应一条记录
                int insert = borrowReturnBooksMapper.insert(borrowReturnBooks);
                if (insert < 1) {
                    try {
                        throw new Exception();
                    } catch (Exception e) {
                        return Result.create(ResultEnum.UPDATE_ERROR, null);
                    }
                }
                if(!redisTemplate.opsForZSet().addIfAbsent("lib:stBookBorrowedCount", bookId, 1)){
                    redisTemplate.opsForZSet().incrementScore("lib:stBookBorrowedCount", bookId, 1);
                }
                redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, -1);

                int year = calendar.get(Calendar.YEAR);
                int week = calendar.get(Calendar.WEEK_OF_YEAR);
                int month = calendar.get(Calendar.MONTH);
                int weekday = calendar.get(Calendar.DAY_OF_WEEK);
                if(!redisTemplate.opsForHash().putIfAbsent("lib:" + year + "." + week, weekday , 1)){
                    redisTemplate.opsForHash().increment("lib:" + year + "." + week, weekday, 1);
                }

                if(!redisTemplate.opsForHash().putIfAbsent("lib:" + year + "&" + month, category, 1)){
                    redisTemplate.opsForHash().increment("lib:" + year + "&" + month, category, 1);
                }
            } else {
                //归还情况下在更新brb记录信息时需判断当前用户是否逾期归还
                Date planTime1 = borrowReturnBooksMapper.getPlanTimeByReaderIdAndBookId(readerId, bookId);
                if(planTime1.before(new Date())){
                    //逾期归还,修改状态，缓存
                    int i1 = borrowReturnBooksMapper.updateStatusAndReturnTime(3, new Date(), readerId, bookId);
                    if (i1 < 1) {
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            return Result.create(ResultEnum.UPDATE_ERROR, null);
                        }
                    }
                    if(!redisTemplate.opsForZSet().addIfAbsent("lib:ViolateRank", readerId, 1)){
                        redisTemplate.opsForZSet().incrementScore("lib:ViolateRank", readerId, 1);
                    }
                } else{
                    //期限内归还
                    int i2 = borrowReturnBooksMapper.updateStatusAndReturnTime(2, new Date(), readerId, bookId);
                    if (i2 < 1) {
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            return Result.create(ResultEnum.UPDATE_ERROR, null);
                        }
                    }
                }
                bookMapper.incrCopyCountById(bookId);
                redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, 1);
            }


            //处理缓存
            redisTemplate.opsForSet().remove("lib:WaitingMessagesList", str);
            redisTemplate.opsForSet().add("lib:FinishedMessagesList", str);
        }
        return Result.create(ResultEnum.UPDATE_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result rejectWaitingApproves(Map<String, Object> map) {
        //获取审批内容、日期及管理员id
        String content = (String) map.get("content");
        String Date = (String) map.get("date");
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
        } catch (ParseException e) {
            return Result.create(ResultEnum.UPDATE_ERROR, null);
        }

        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(date)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        //获取brb实体需要的参数

        Integer operation = null;
        Integer readerId = null;
        Integer rEndIndex = null;

        //预处理提取信息
        if(content.contains("借阅")){
            operation = 0;
            rEndIndex = content.indexOf("借");
        } else{
            operation = 1;
            rEndIndex = content.indexOf("归");
        }

        String loginName = content.substring(0, rEndIndex);
        readerId = readerMapper.selectReaderByLoginName(loginName).get(0).getId();


        //从缓存中获取到对应信息，从中获取到bookId和借阅的月数
        Integer bookId = null;
        Integer borrowTime = null;
        Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*" + loginName + "*" + Date + "*").build());

        String str = new String();
        try {
            str = scan.next().toString();
        } catch (NoSuchElementException e){
            return Result.create(ResultEnum.UPDATE_ERROR,null);
        }
        int t = str.lastIndexOf("&") + 1;
        int j = str.lastIndexOf("&", t-2);
        bookId = Integer.parseInt(str.substring(t));
        borrowTime = Integer.parseInt(str.substring(j + 1, t - 1));
        if(bookId == null || borrowTime == null) return Result.create(ResultEnum.DATA_IS_NULL, null);




        //修改数据库approve表对应记录，同时不要忘记修改缓存
        int i = approveMapper.updateStatusByBookIdAndReaderIdAndDateAndOperation(-1, bookId, readerId, operation, date);
        if(i < 1) return Result.create(ResultEnum.UPDATE_ERROR, null);


        //处理缓存
        redisTemplate.opsForSet().remove("lib:WaitingMessagesList", str);
        redisTemplate.opsForSet().add("lib:FinishedMessagesList", str + ":reject");
        return Result.create(ResultEnum.UPDATE_SUCCESS,null);
    }


    @Override
    public Result getAllFinishedApproves(Integer pageNo, Integer pageSize) {
        if(pageNo == null || pageSize == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        List<String> members = new ArrayList<>();
        List<ApproveModel> membersList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Integer WCount = 0;
        Integer FCount = 0;
        //需要返回：未读消息和已读消息的条数，以及所有未读消息
        PageHelper.startPage(pageNo, pageSize);
        List<approve> list = approveMapper.selectAllFinishedMessages();
        PageInfo pageInfo = new PageInfo(list, 5);

        for(Object obj : pageInfo.getList()){
            approve approve = (approve) obj;
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(approve.getDate());
            Cursor scan = redisTemplate.opsForSet().scan("lib:FinishedMessagesList", ScanOptions.scanOptions().match("*" + time + "*" + approve.getBookId() + "*").build());
            if(scan.hasNext()) {
                String str = scan.next().toString();
                members.add(str);
            }
        }

        WCount = redisTemplate.opsForSet().size("lib:WaitingMessagesList").intValue();
        FCount = redisTemplate.opsForSet().size("lib:FinishedMessagesList").intValue();
        if(members == null || WCount == null || FCount == null) return Result.create(ResultEnum.QUERY_ERROR, null);

        for(String string : members) {
            String[] contain = string.split("&");
            String content = contain[0];
            String Date = contain[1];
            ApproveModel approveModel = new ApproveModel(content,Date);
            membersList.add(approveModel);
        }

        pageInfo.setList(membersList);


        map.put("list", pageInfo);
        map.put("FinishedCount", FCount);
        map.put("WaitingCount", WCount);

        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Transactional
    @Override
    public Result deleteFinishedApproves(Map<String, Object> map) {
        //获取审批内容、日期及管理员id
        String content = (String) map.get("content");
        String Date = (String) map.get("date");
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
        } catch (ParseException e) {
            return Result.create(ResultEnum.DELETE_ERROR, null);
        }

        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(date)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        //获取brb实体需要的参数

        Integer operation = null;
        Integer readerId = null;
        Integer rEndIndex = null;

        //预处理提取信息
        if(content.contains("借阅")){
            operation = 0;
            rEndIndex = content.indexOf("借");
        } else{
            operation = 1;
            rEndIndex = content.indexOf("归");
        }

        String loginName = content.substring(0, rEndIndex);
        readerId = readerMapper.selectReaderByLoginName(loginName).get(0).getId();

        //从缓存中获取到对应信息，从中获取到bookId和借阅的月数
        Integer bookId = null;
        Integer borrowTime = null;
        Cursor scan = redisTemplate.opsForSet().scan("lib:FinishedMessagesList", ScanOptions.scanOptions().match("*" + loginName + "*" + Date + "*").build());

        String str1;
        try {
            str1 = scan.next().toString();
        } catch (NoSuchElementException e){
            return Result.create(ResultEnum.DELETE_ERROR,null);
        }
        String str = null;
        if(str1.endsWith(":reject")) str = str1.substring(0, str1.length()-7);
        else str = str1;
        int t = str.lastIndexOf("&") + 1;
        int j = str.lastIndexOf("&", t-2);
        bookId = Integer.parseInt(str.substring(t));
        borrowTime = Integer.parseInt(str.substring(j + 1, t - 1));
        if(bookId == null || borrowTime == null) return Result.create(ResultEnum.DATA_IS_NULL, null);


        int i = approveMapper.deleteFinishedByParam(bookId, readerId, operation, date, 0);
        if(i < 1) return Result.create(ResultEnum.DELETE_ERROR,null);

        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", str1);
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result deleteAllFinishedApproves(Map<String, Object> map) {
        List<ApproveModel> list = new ArrayList<>();
        Set<String> members = redisTemplate.opsForSet().members("lib:FinishedMessagesList");
        if(members == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        for(String member : members){
            String[] split = member.split("&");
            String content = split[0];
            String date = split[1];
            if(StringUtils.isEmpty(content) || StringUtils.isEmpty(date))  return Result.create(ResultEnum.DATA_IS_NULL, null);
            ApproveModel approveModel = new ApproveModel();
            approveModel.setContent(content);
            approveModel.setDate(date);
            list.add(approveModel);
        }
        for(ApproveModel approveModel : list){
            String content = approveModel.getContent();
            String Date = approveModel.getDate();
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Date);
            } catch (ParseException e) {
                return Result.create(ResultEnum.DELETE_ERROR, null);
            }

            if (StringUtils.isEmpty(content) || StringUtils.isEmpty(date)) return Result.create(ResultEnum.DATA_IS_NULL,null);

            Integer operation = null;
            Integer readerId = null;
            Integer rEndIndex = null;

            //预处理提取信息
            if(content.contains("借阅")){
                operation = 0;
                rEndIndex = content.indexOf("借");
            } else{
                operation = 1;
                rEndIndex = content.indexOf("归");
            }

            String loginName = content.substring(0, rEndIndex);
            readerId = readerMapper.selectReaderByLoginName(loginName).get(0).getId();

            //从缓存中获取到对应信息，从中获取到bookId和借阅的月数
            Integer bookId = null;
            Cursor scan = redisTemplate.opsForSet().scan("lib:FinishedMessagesList", ScanOptions.scanOptions().match("*" + loginName + "*" + Date + "*").build());

            String str = new String();
            try {
                str = scan.next().toString();
            } catch (NoSuchElementException e){
                return Result.create(ResultEnum.DELETE_ERROR,null);
            }
            int t = str.lastIndexOf("&") + 1;
            bookId = Integer.parseInt(str.substring(t));
            if(bookId == null) return Result.create(ResultEnum.DATA_IS_NULL, null);


            int i = approveMapper.deleteFinishedByParam(bookId, readerId, operation, date, 0);
            try {
                if(i < 1)
                    throw new Exception();
            } catch (Exception e){
                System.out.println("删除未生效");
                return Result.create(ResultEnum.DELETE_ERROR,null);
            }

            redisTemplate.opsForSet().remove("lib:FinishedMessagesList", str);
        }
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }

    @Override
    public Result getAllBRB(Integer pageNo, Integer pageSize, String keyWord) {
        if(pageNo == null || pageSize == null || keyWord == null) return Result.create(ResultEnum.DATA_IS_NULL,null);
        PageHelper.startPage(pageNo, pageSize);
        List<brbEntity> list = borrowReturnBooksMapper.selectAllByKeyWord(keyWord);
        if(list == null) return Result.create(ResultEnum.QUERY_ERROR,null);
        PageInfo<brbEntity> pageInfo = new PageInfo<>(list, 5);
        return Result.create(ResultEnum.QUERY_SUCCESS,pageInfo);
    }

    @Transactional
    @Override
    public Result deleteBRB(Integer id) {
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        borrowReturnBooks borrowReturnBooks = borrowReturnBooksMapper.selectByPrimaryKey(id);
        if(borrowReturnBooks == null) return Result.create(ResultEnum.DELETE_ERROR, null);
        if(borrowReturnBooks.getStatus() == 1) return Result.create(ResultEnum.READER_NOT_FINISH,null);
        int i = borrowReturnBooksMapper.deleteByPrimaryKey(id);
        if(i < 1) return Result.create(ResultEnum.DELETE_ERROR, null);
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result batchDeleteBRBs(Map<String, Object> map) {
        List<Integer> ids = (List<Integer>) map.get("ids");
        if (ids == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        for(Integer id : ids){
            borrowReturnBooks borrowReturnBooks = borrowReturnBooksMapper.selectByPrimaryKey(id);
            try {
                if (borrowReturnBooks == null) throw new NoSuchElementException();
                if (borrowReturnBooks.getStatus() == 1) throw new NumberFormatException();
            } catch (NoSuchElementException E){
                System.out.println("找不到id");
                return Result.create(ResultEnum.DELETE_ERROR, null);
            } catch (NumberFormatException E){
                System.out.println("搞什么飞机");
                return Result.create(ResultEnum.READER_NOT_FINISH, null);
            }
            int i = borrowReturnBooksMapper.deleteByPrimaryKey(id);
            try {
                if(i < 1) throw new Exception();
            } catch (Exception e){
                System.out.println("删除时报错");
                return Result.create(ResultEnum.DELETE_ERROR, null);
            }
        }
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }
}
