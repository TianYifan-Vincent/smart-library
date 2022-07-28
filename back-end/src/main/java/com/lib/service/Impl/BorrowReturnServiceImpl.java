package com.lib.service.Impl;

import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.config.RedisConfig;
import com.lib.controller.vo.ApproveModel;
import com.lib.controller.vo.ReaderRecordVO;
import com.lib.entity.Book;
import com.lib.entity.approve;
import com.lib.entity.borrowReturnBooks;
import com.lib.mapper.BookMapper;
import com.lib.mapper.ReaderMapper;
import com.lib.mapper.approveMapper;
import com.lib.mapper.borrowReturnBooksMapper;
import com.lib.service.BorrowReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BorrowReturnServiceImpl implements BorrowReturnService {
/**
 * @ClassName BorrowReturnServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 14:58
 * @Version 1.0
 **/

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public approveMapper approveMapper;

    @Transactional
    @Override
    public Result addApprove(Map<String,Object> map) {
        //借书：副本为0无法借阅，违规记录超过五次无法借阅
        Integer bookId = (Integer) map.get("bookId");
        Integer readerId = (Integer) map.get("readerId");
        Integer time = (Integer) map.get("time");
        Date tempDate = new Date();
        String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tempDate);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if(bookId == null || readerId == null || time == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        Book book = bookMapper.selectByPrimaryKey(bookId);

        //如果该书副本数为0，则说明书已接完，无法进行借阅
        if(book.getBookCopyNumber() < 1) return Result.create(ResultEnum.BOOK_COPY_ZERO, null);
        //如果该用户违规次数超出5次，则不允许借阅
        Boolean aBoolean = redisTemplate.opsForZSet().addIfAbsent("lib:ReaderVioCount", "R" + readerId, 0);
        if(aBoolean == false){
            int i = redisTemplate.opsForZSet().score("lib:ReaderVioCount", "R" + readerId).intValue();
            if(i >= 5) return Result.create(ResultEnum.VIO_TOO_MUCH, null);
        }
        //如果该用户已借了五本书，则无法进行借阅
        int i1 = borrowReturnBooksMapper.selectCountByReaderId(readerId);
        if(i1 >= 5) return Result.create(ResultEnum.BEYOND_MAX_BORROW, null);
        //如果该用户存在逾期未还的书，则不允许借阅
        int i2 = borrowReturnBooksMapper.selectOverdueCountByReaderId(readerId, date);
        if(i2 > 0) return Result.create(ResultEnum.OVERDUE_EXIST, null);
        //如果都ok，就添加一个新的审批，对书的副本数进行-1
        try {
            String title = bookMapper.selectByPrimaryKey(bookId).getTitle();
            String readerName = readerMapper.selectByPrimaryKey(readerId).getLoginName();
            String content = readerName + "借阅《" + title + "》审批中&" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "&" + time + "&" + bookId;
            if(redisTemplate.opsForSet().isMember("lib:WaitingMessagesList", content)){
                return Result.create(ResultEnum.APPROVE_EXIST,null);
            }

            approve approve = new approve();
            approve.setBookId(bookId);
            approve.setStatus(0);
            approve.setReaderId(readerId);
            approve.setDate(date);
            approve.setOperation(0);
            //副本数减一，数据库更新
            int insert = approveMapper.insert(approve);

            //缓存加入记录
            Long add = redisTemplate.opsForSet().add("lib:WaitingMessagesList", content);
            if(insert < 1 || add < 1) throw new Exception();
        } catch (Exception e){
            e.printStackTrace();
            return Result.create(ResultEnum.UPDATE_ERROR, null);
        }

        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result returnBook(Map<String,Object> map) {
        //还书审批
        Integer id = (Integer) map.get("id");
        Integer readerId = (Integer) map.get("readerId");
        Date tempDate = new Date();
        String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tempDate);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(id == null || readerId == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        borrowReturnBooks borrowReturnBooks = borrowReturnBooksMapper.selectByPrimaryKey(id);

        //如果都ok，就添加一个新的审批，
        try {
            String title = bookMapper.selectByPrimaryKey(borrowReturnBooks.getBookId()).getTitle();
            String readerName = readerMapper.selectByPrimaryKey(readerId).getLoginName();
            String content = readerName + "归还《" + title + "》审批中&" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "&" + 404 + "&" + borrowReturnBooks.getBookId();
            if(redisTemplate.opsForSet().isMember("lib:WaitingMessagesList", content)){
                return Result.create(ResultEnum.APPROVE_EXIST,null);
            }

            approve approve = new approve();
            approve.setBookId(borrowReturnBooks.getBookId());
            approve.setStatus(0);
            approve.setReaderId(readerId);
            approve.setDate(date);
            approve.setOperation(1);
            //副本数减一，数据库更新
            int insert = approveMapper.insert(approve);



            //缓存加入记录
            Long add = redisTemplate.opsForSet().add("lib:WaitingMessagesList", content);
            if(insert < 1 || add < 1) throw new Exception();
        } catch (Exception e){
            e.printStackTrace();
            return Result.create(ResultEnum.UPDATE_ERROR, null);
        }

        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }

    @Transactional
    @Override
    public Result deleteBorrowRecord(Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String title = (String) map.get("title");
        String publisher = (String) map.get("publisher");
        Date borrowTime = null;
        Date planTime = null;
        try {
            borrowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) map.get("borrowTime"));
            planTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) map.get("planTime"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Integer status = (Integer) map.get("status");

        if(id == null || StringUtils.isEmpty(title) || StringUtils.isEmpty(publisher) || borrowTime == null || planTime ==null || status == null)
            return Result.create(ResultEnum.DATA_IS_NULL, null);

        int i = borrowReturnBooksMapper.deleteByPrimaryKey(id);
        if(i < 1) return Result.create(ResultEnum.DELETE_ERROR, null);

        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }

    @Override
    public Result batchDelete(Map<String, Object> map) {
        List<Integer> ids = (List<Integer>) map.get("ids");
        if(ids == null) return Result.create(ResultEnum.DATA_IS_NULL, null);

        for(Integer id : ids){
            int i = borrowReturnBooksMapper.deleteByPrimaryKey(id);
            try {
                if(i < 1) throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.create(ResultEnum.DELETE_ERROR, null);
            }
        }
        return Result.create(ResultEnum.DELETE_SUCCESS, null);
    }
}
