package com.lib.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.controller.vo.BookShowVO;
import com.lib.controller.vo.CategoryBook;
import com.lib.controller.vo.ReaderRecordVO;
import com.lib.entity.*;
import com.lib.mapper.BookMapper;
import com.lib.mapper.ReaderMapper;
import com.lib.mapper.bookCategoryMapper;
import com.lib.mapper.borrowReturnBooksMapper;
import com.lib.service.LibHomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.PushBuilder;
import java.util.*;

@Service
public class LibHomePageServiceImpl implements LibHomePageService {
/**
 * @ClassName LibHomePageServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 1:49
 * @Version 1.0
 **/
    @Autowired
    public bookCategoryMapper bookCategoryMapper;

    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @Override
    public Result getHomePage(Integer id) {
        long begin = System.currentTimeMillis();

        Set<Integer> range = redisTemplate.opsForZSet().reverseRange("lib:stBookBorrowedCount", 0, 4);
        Set<ZSetOperations.TypedTuple> set = redisTemplate.opsForZSet().rangeWithScores("lib:TypeBookCount", 0, -1);
        Set<Integer> range1 = redisTemplate.opsForZSet().range("lib:stNewBook", 0, -1);

        //三个模块：为你推荐书籍，热门书籍，最新书籍
        Map<String, Object> map = new HashMap<>();
        if (id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);

        //热门书籍
        int size = range.size();
        long sqlbegin = System.currentTimeMillis();
        List<BookShowVO> hotBooks = new ArrayList<>();
        if(size != 0) hotBooks = bookMapper.batchSelectByPrimaryKey(range);
        if(hotBooks == null) return Result.create(ResultEnum.QUERY_ERROR, null);
        long sqlend = System.currentTimeMillis();
        System.out.println("查询数据库花费:" + (sqlend - sqlbegin) + "毫秒");
        int cnt = 0;
        for (ZSetOperations.TypedTuple t : set) {
            cnt += t.getScore();
        }
        Set<Integer> cacheSet1 = new HashSet<>();
        for (int i = 1; i <= 5 - size - cacheSet1.size(); i++) {
            int j = RandomUtil.randomInt(1, cnt);
            while (range.contains(j)||cacheSet1.contains(j)) j = RandomUtil.randomInt(1, cnt);
            cacheSet1.add(j);
        }
        List<BookShowVO> cacheList1 = new ArrayList<>();
        if(cacheSet1.size() != 0) cacheList1 = bookMapper.batchSelectByPrimaryKey(cacheSet1);
        hotBooks.addAll(cacheList1);
        map.put("hotBooks", hotBooks);
        long end1 = System.currentTimeMillis();
        System.out.println("热门书籍花费:" + (end1 - begin) + "毫秒");


        //最新书籍
        long begin1 = System.currentTimeMillis();
        List<BookShowVO> newBooks = bookMapper.batchSelectByPrimaryKey(range1);
        map.put("newBooks", newBooks);
        long end2 = System.currentTimeMillis();
        System.out.println("最新书籍花费:" + (end2 - begin1) + "毫秒");


        //为你推荐书籍
        long begin2 = System.currentTimeMillis();
        List<BookShowVO> recBooks = new ArrayList<>();
        List<Integer> allCategoryBorrowedByReaderId = borrowReturnBooksMapper.getAllCategoryBorrowedByReaderId(id);
        List<Book> allBookByCategories = new ArrayList<>();
        if(allCategoryBorrowedByReaderId != null || allCategoryBorrowedByReaderId.size() != 0){
            allBookByCategories = bookMapper.getAllBookByCategories(allCategoryBorrowedByReaderId);
        }


        int size1 = allBookByCategories.size();
        int size2 = allCategoryBorrowedByReaderId.size();
        Set<Integer> set1 = new HashSet<>();
        if(size1 > 10 && size2 != 0){
            while (set1.size() < 5){
                int j = RandomUtil.randomInt(0, allBookByCategories.size() - 1);
                if(set1.contains(j)) continue;
                set1.add(j);
                Book book = allBookByCategories.get(j);
                BookShowVO bookShowVO = new BookShowVO(book.getId(),book.getCoverImg(),book.getTitle(),book.getAuthor(),book.getIsbn(),book.getPublisher());
                recBooks.add(bookShowVO);
            }
        } else {
            Set<Integer> cacheSet2 = new HashSet<>();
            while (cacheSet2.size() < 5) {
                int j = RandomUtil.randomInt(1, cnt);
                cacheSet2.add(j);
            }
            List<BookShowVO> cacheList2 = bookMapper.batchSelectByPrimaryKey(cacheSet2);
            recBooks.addAll(cacheList2);
        }
        long end3 = System.currentTimeMillis();
        System.out.println("推荐书籍花费:" + (end3 - begin2) + "毫秒");
        map.put("recBooks", recBooks);
        long end = System.currentTimeMillis();
        System.out.println("总共花费:" + (end - begin) + "毫秒");
        return Result.create(ResultEnum.QUERY_SUCCESS, map);
    }

    @Override
    public Result getBookInfo(Integer id) {
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL,null);
        Book book = bookMapper.selectByPrimaryKey(id);
        if(book == null) return Result.create(ResultEnum.QUERY_ERROR,null);
        return Result.create(ResultEnum.QUERY_SUCCESS, book);
    }

    @Override
    public Result searchBook(Integer pageNo, Integer pageSize, String keyWord, String category) {
        if(pageNo == null || pageSize == null || StringUtils.isEmpty(keyWord)){
            return Result.create(ResultEnum.NOT_NULLABLE, null);
        }

        Map<String, Integer> categoryName = new HashMap<>();
        Map<Integer, String> nameCategory = new HashMap<>();
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        List<String> nameList = new ArrayList<>();
        Map<String ,Object> returnMap = new HashMap<>();

        for(bookCategory bc : bookCategories){
            categoryName.put(bc.getCategoryName(), bc.getId());
            nameCategory.put(bc.getId(), bc.getCategoryName());
            nameList.add(bc.getCategoryName());
        }

        returnMap.put("categoryName", nameList);

        Integer cid = null;
        if(!category.equals("null")){
            cid = categoryName.get(category);
            if(cid == null){
                return Result.create(ResultEnum.CATEGORY_NOT_EXIST,returnMap);
            }
        }

        PageHelper.startPage(pageNo, pageSize);
        List<Book> books = bookMapper.selectAllByKeyWordAndCategory(keyWord, cid);

        List<BookShowVO> list = new ArrayList<>();
        for(Book b : books){
            BookShowVO bookShowVO = new BookShowVO(b.getId(), b.getCoverImg(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublisher());
            list.add(bookShowVO);
        }
        PageInfo pageInfo = new PageInfo<>(books,5);
        pageInfo.setList(list);

        returnMap.put("pageInfo", pageInfo);
        return Result.create(ResultEnum.QUERY_SUCCESS, returnMap);
    }

    @Override
    public Result myRecord(Integer id, Integer pageNo, Integer pageSize, String keyWord) {
        if(pageNo == null || pageSize == null || StringUtils.isEmpty(keyWord)){
            return Result.create(ResultEnum.NOT_NULLABLE, null);
        }
        PageHelper.startPage(pageNo,pageSize);
        List<ReaderRecordVO> readerRecordByReaderId = borrowReturnBooksMapper.getReaderRecordByReaderIdAndKeyWord(id, keyWord);
        if(readerRecordByReaderId == null) return Result.create(ResultEnum.QUERY_ERROR, null);
        PageInfo<ReaderRecordVO> pageInfo = new PageInfo<>(readerRecordByReaderId);
        return Result.create(ResultEnum.QUERY_SUCCESS, pageInfo);
    }

    @Override
    public Result getReaderInfo(Integer id) {
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);
        Reader reader = readerMapper.selectByPrimaryKey(id);
        if(reader == null) return Result.create(ResultEnum.QUERY_ERROR, null);
        return Result.create(ResultEnum.QUERY_SUCCESS, reader);
    }

    @Override
    public Result editReaderInfo(Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String oldPwd = (String) map.get("oldPwd");
        String newPwd = (String) map.get("newPwd");
        String readerName = (String) map.get("readerName");
        String newMd5Pwd = "";
        String gender = (String) map.get("gender");
        Integer age = (Integer) map.get("age");
        String email = (String) map.get("email");
        String phone = (String) map.get("phone");

        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn)";
        String newSalt = RandomUtil.randomString(6);
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL, null);

        if((StringUtils.isEmpty(oldPwd)&&!StringUtils.isEmpty(newPwd)) || (!StringUtils.isEmpty(oldPwd)&&StringUtils.isEmpty(newPwd)))
            return Result.create(ResultEnum.BAD_FORMAT, null);
        if (!StringUtils.isEmpty(email)&&!email.matches(tegex))
            return Result.create(ResultEnum.WRONG_FORMAT_EMAIL, null);

        Reader reader = readerMapper.selectByPrimaryKey(id);
        String oldEmail = reader.getEmail();
        if(!StringUtils.isEmpty(oldPwd)&&!StringUtils.isEmpty(newPwd)){
            String oldSalt = reader.getSalt();
            String tempPwd = SecureUtil.md5(oldPwd + oldSalt);
            if(!tempPwd.equals(reader.getPassword())){
                return Result.create(ResultEnum.WRONG_CODE, null);
            }
            newMd5Pwd = SecureUtil.md5(newPwd + newSalt);
        }
        int i = readerMapper.updateReaderInfo(id, newSalt, newMd5Pwd, gender, age, email, readerName, phone);
        if(i < 1) return Result.create(ResultEnum.UPDATE_ERROR,null);

        if(!StringUtils.isEmpty(email)) {
            if (!StringUtils.isEmpty(oldEmail)) {
                redisTemplate.opsForSet().remove("lib:ReaderEmail", oldEmail);
            }
            redisTemplate.opsForSet().add("lib:ReaderEmail", email);
        }
        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }
}
