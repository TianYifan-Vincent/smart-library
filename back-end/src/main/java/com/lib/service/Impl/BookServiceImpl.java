package com.lib.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.controller.vo.CategoryBook;
import com.lib.entity.*;
import com.lib.mapper.*;
import com.lib.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookServiceImpl implements BookService {
/**
 * @ClassName BookServiceImpl
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/15 14:28
 * @Version 1.0
 **/
    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public bookCategoryMapper bookCategoryMapper;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @Autowired
    public ReaderMapper readerMapper;

    @Autowired
    public approveMapper approveMapper;

    @Autowired
    public RedisTemplate redisTemplate;



    @Override
    public Result getAllBooks(Integer pageNo, Integer pageSize, String keyWord, String category) {
        long begin = System.currentTimeMillis();
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
        List<CategoryBook> categoryBooks = new ArrayList<>();
        for(Book b : books){
            CategoryBook categoryBook = new CategoryBook();
            categoryBook.setId(b.getId());
            categoryBook.setTitle(b.getTitle());
            categoryBook.setIsbn(b.getIsbn());
            categoryBook.setCoverImg(b.getCoverImg());
            categoryBook.setDescription(b.getDescription());
            categoryBook.setAuthor(b.getAuthor());
            categoryBook.setPrice(b.getPrice());
            categoryBook.setBookCopyNumber(b.getBookCopyNumber());
            categoryBook.setPublisher(b.getPublisher());
            categoryBook.setPublishDate(b.getPublishDate());
            categoryBook.setUserId(b.getUserId());
            categoryBook.setCreatedTime(b.getCreatedTime());
            categoryBook.setCategory(nameCategory.get(b.getCategory()));

            categoryBooks.add(categoryBook);
        }
        PageInfo pageInfo = new PageInfo<>(books,5);
        pageInfo.setList(categoryBooks);

        returnMap.put("pageInfo", pageInfo);
        long end = System.currentTimeMillis();
        System.out.println("总共花费:"+(end-begin)+"ms");
        return Result.create(ResultEnum.QUERY_SUCCESS, returnMap);

    }

    @Transactional
    @Override
    public Result addBook(Map<String, Object> map) {
        Map<String, Integer> categoryName = new HashMap<>();
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            categoryName.put(bc.getCategoryName(), bc.getId());
        }

        String title = (String) map.get("title");
        String isbn = (String) map.get("isbn");
        String urlCover = (String) map.get("coverImg");
        String description = (String) map.get("description");
        String author = (String) map.get("author");
        String priceStr = (String) map.get("price");
        String cateName = (String) map.get("category");
        String bookCopyNumberStr = (String) map.get("bookCopyNumber");
        String publisher = (String) map.get("publisher");
        String publishDate = (String) map.get("publishDate");
        Integer userId = Integer.valueOf((String) map.get("userId"));
        Date createdTime = new Date();

        String pattern = "[0-9]{4}年((0)[1-9]|(1)[0-2])月";

        Integer bookCopyNumber = null;
        Double priceDouble = null;
        BigDecimal price = null;

        if(StringUtils.isEmpty(title) || StringUtils.isEmpty(isbn) || StringUtils.isEmpty(urlCover) || StringUtils.isEmpty(author) || StringUtils.isEmpty(priceStr) || StringUtils.isEmpty(cateName) || StringUtils.isEmpty(bookCopyNumberStr) || StringUtils.isEmpty(publisher) || StringUtils.isEmpty(publishDate))
            return Result.create(ResultEnum.DATA_IS_NULL,null);
        Integer category = categoryName.get(cateName);
        bookCopyNumber = Integer.valueOf(bookCopyNumberStr);
        try{

            priceDouble = Double.valueOf(priceStr);
            price = new BigDecimal(priceDouble);
        }catch (NumberFormatException e){
            return Result.create(ResultEnum.BAD_FORMAT,null);
        }

        if(title.length() > 50 || price.compareTo(new BigDecimal(0)) < 0 || bookCopyNumber < 0)
            return Result.create(ResultEnum.BAD_FORMAT,null);

        List<Book> books = bookMapper.selectByISBN(isbn);
        if(books.size() > 0) return Result.create(ResultEnum.ISBN_EXIST,null);

        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setCoverImg(urlCover);
        book.setDescription(description);
        book.setAuthor(author);
        book.setPrice(price);
        book.setCategory(category);
        book.setBookCopyNumber(bookCopyNumber);
        book.setPublisher(publisher);
        book.setPublishDate(publishDate);
        book.setUserId(userId);
        book.setCreatedTime(createdTime);

        int insert = bookMapper.insert(book);
        if(insert < 1) return Result.create(ResultEnum.INSERT_ERROR,null);

        redisTemplate.opsForZSet().incrementScore("lib:TypeBookCount", "C"+category, 1);
        redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, bookCopyNumber);

        redisTemplate.opsForZSet().add("lib:stNewBook", book.getId(), book.getCreatedTime().getTime());
        Set<Integer> set = redisTemplate.opsForZSet().range("lib:stNewBook", 0, -1);
        for(Integer o : set){
            redisTemplate.opsForZSet().remove("lib:stNewBook", o);
            break;
        }


        return Result.create(ResultEnum.INSERT_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result editBook(Map<String, Object> map) {
        Map<String, Integer> categoryName = new HashMap<>();
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            categoryName.put(bc.getCategoryName(), bc.getId());
        }

        Integer id = Integer.valueOf((String) map.get("id"));
        String title = (String) map.get("title");
        String isbn = (String) map.get("isbn");
        String urlCover = (String) map.get("coverImg");
        String author = (String) map.get("author");
        String priceStr = (String) map.get("price").toString();
        String cateName = (String) map.get("category");
        Integer category = categoryName.get(cateName);
        String bookCopyNumberStr = (String) map.get("bookCopyNumber");
        String publisher = (String) map.get("publisher");
        String publishDate = (String) map.get("publishDate");
        Integer userId = Integer.valueOf((String) map.get("userId"));
        String description = (String) map.get("description");
        Date createdTime;


        Book book = bookMapper.selectByPrimaryKey(id);
        Integer originCategory = book.getCategory();
        Long i = borrowReturnBooksMapper.checkCountApproveAndBorrow(book.getId());
        if(i > 0) return Result.create(ResultEnum.UPDATE_ERROR, null);

        createdTime = book.getCreatedTime();

        String pattern = "[0-9]{4}年((0)[1-9]|(1)[0-2])月";

        Integer bookCopyNumber = null;
        Double priceDouble = null;
        BigDecimal price = null;

        if(StringUtils.isEmpty(title) || StringUtils.isEmpty(isbn) || StringUtils.isEmpty(urlCover) || StringUtils.isEmpty(author) || StringUtils.isEmpty(priceStr) || StringUtils.isEmpty(category) || StringUtils.isEmpty(bookCopyNumberStr) || StringUtils.isEmpty(publisher) || StringUtils.isEmpty(publishDate) || userId  == null)
            return Result.create(ResultEnum.DATA_IS_NULL,null);

        bookCopyNumber = Integer.valueOf(bookCopyNumberStr);
        try{

            priceDouble = Double.valueOf(priceStr);
            price = new BigDecimal(priceDouble);
        }catch (NumberFormatException e){
            return Result.create(ResultEnum.BAD_FORMAT,null);
        }

        if(title.length() > 40 || price.compareTo(new BigDecimal(0)) < 0 || bookCopyNumber < 0)
            return Result.create(ResultEnum.BAD_FORMAT,null);

        List<Book> books = bookMapper.selectByISBN(isbn);
        if(books.size() > 0) {
            Book temp = books.get(0);
            if(!temp.getId().equals(id) && temp.getIsbn().equals(isbn)) return Result.create(ResultEnum.ISBN_EXIST,null);
        }

        int update = bookMapper.editBook(id, title, isbn, urlCover, author, price, category, bookCopyNumber, publisher, publishDate, userId, createdTime, description);
        if(update < 1) return Result.create(ResultEnum.UPDATE_ERROR,null);

        if(!category.equals(originCategory)){
            redisTemplate.opsForZSet().incrementScore("lib:TypeBookCount", "C"+originCategory,-1);
            redisTemplate.opsForZSet().incrementScore("lib:TypeBookCount", "C"+category,1);

        }
        redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + originCategory , -book.getBookCopyNumber());
        redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + category, bookCopyNumber);
        return Result.create(ResultEnum.UPDATE_SUCCESS, null);
    }

    @Override
    public Result deleteBook(Integer id, String category) {
        /*
        *  删除流程：删除一本书需要检验书是否存在任何用户的借阅记录，若存在则不可删除。
        *   对书本进行删除的同时应删除相关委托及所有对应的缓存记录：书本总数统计、对应类型数量统计、委托总数统计
        **/
        Map<String, Integer> categoryName = new HashMap<>();
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            categoryName.put(bc.getCategoryName(), bc.getId());
        }
        int cate = categoryName.get(category);

        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL,null);

        List<borrowReturnBooks> borrowReturnBooks = borrowReturnBooksMapper.selectByBookId(id);
        if(borrowReturnBooks.size() > 0) return Result.create(ResultEnum.READER_NOT_FINISH,null);

        Book book = bookMapper.selectByPrimaryKey(id);
        List<approve> list = approveMapper.selectByBookId(id);

        int delete = bookMapper.deleteByPrimaryKey(id);
        if(delete < 1) return Result.create(ResultEnum.DELETE_ERROR,null);


        //删除对应缓存
        redisTemplate.opsForZSet().remove("lib:stBookBorrowedCount", book.getId());
        redisTemplate.opsForZSet().incrementScore("lib:TypeBookCount", "C"+cate ,-1);
        redisTemplate.opsForValue().decrement("lib:WaitingMessages", (long) list.size());
        redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + book.getCategory() , -book.getBookCopyNumber());


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

        //更新最新书籍
        List<Book> books = bookMapper.selectAllOrderByTime();
        for (int i = 0; i < 5; i++){
            redisTemplate.opsForZSet().add("lib:stNewBook", books.get(i).getId(), books.get(i).getCreatedTime().getTime());
        }

        return Result.create(ResultEnum.DELETE_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result deleteBooks(Map<String, Object> map) {
        List<Integer> ids = (List<Integer>) map.get("ids");
        if(ids == null) return Result.create(ResultEnum.DATA_IS_NULL,null);

        Map<String, Integer> categoryName = new HashMap<>();
        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            categoryName.put(bc.getCategoryName(), bc.getId());
        }

        for(int id : ids){
            List<Integer> clist = bookMapper.selectCategoryByPrimaryKey(id);
            Integer cate = clist.get(0);

            List<borrowReturnBooks> borrowReturnBooks = borrowReturnBooksMapper.selectByBookId(id);
            if(borrowReturnBooks.size() > 0) return Result.create(ResultEnum.READER_NOT_FINISH,null);

            Book book = bookMapper.selectByPrimaryKey(id);
            List<approve> list = approveMapper.selectByBookId(id);

            int delete = bookMapper.deleteByPrimaryKey(id);
            if(delete < 1) return Result.create(ResultEnum.DELETE_ERROR,null);

            //删除对应缓存
            redisTemplate.opsForZSet().remove("lib:stBookBorrowedCount", book.getId());
            redisTemplate.opsForZSet().incrementScore("lib:TypeBookCount", "C"+cate ,-1);
            redisTemplate.opsForValue().decrement("lib:WaitingMessages", (long) list.size());
            redisTemplate.opsForHash().increment("lib:TypeBookCopyCount", "C" + book.getCategory() , -book.getBookCopyNumber());


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

            //更新最新书籍
            List<Book> books = bookMapper.selectAllOrderByTime();
            for (int i = 0; i < 5; i++){
                redisTemplate.opsForZSet().add("lib:stNewBook", books.get(i).getId(), books.get(i).getCreatedTime().getTime());
            }

            return Result.create(ResultEnum.DELETE_SUCCESS,null);
        }
        return Result.create(ResultEnum.DELETE_SUCCESS,null);
    }

    @Override
    public Result getAllBookCategory(Integer pageNo, Integer pageSize, String keyWord) {
        if(pageNo == null || pageSize == null || StringUtils.isEmpty(keyWord)){
            return Result.create(ResultEnum.NOT_NULLABLE, null);
        }

        PageHelper.startPage(pageNo, pageSize);
        List<bookCategory> bookCategories = bookCategoryMapper.selectAllByKeyWord(keyWord);
        PageInfo<bookCategory> pageInfo = new PageInfo<>(bookCategories);
        return Result.create(ResultEnum.QUERY_SUCCESS, pageInfo);
    }

    @Transactional
    @Override
    public Result editBookCategory(Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String category = (String) map.get("categoryName");
        if (StringUtils.isEmpty(category) || id == null) return Result.create(ResultEnum.DATA_IS_NULL,null);

        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            if(bc.getId() != id && bc.getCategoryName().equals(category))
                return Result.create(ResultEnum.CATEGORY_IS_EXIST,null);
        }

        int i = bookCategoryMapper.updateNameById(category, id);
        if(i < 1) return Result.create(ResultEnum.UPDATE_ERROR,null);

        return Result.create(ResultEnum.UPDATE_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result addBookCategory(Map<String, Object> map) {
        String category = (String) map.get("categoryName");
        if (StringUtils.isEmpty(category)) return Result.create(ResultEnum.DATA_IS_NULL,null);

        List<bookCategory> bookCategories = bookCategoryMapper.selectAll();
        for(bookCategory bc : bookCategories){
            if(bc.getCategoryName().equals(category))
                return Result.create(ResultEnum.CATEGORY_IS_EXIST,null);
        }

        bookCategory bookCategory = new bookCategory();
        bookCategory.setCategoryName(category);
        int insert = bookCategoryMapper.insert(bookCategory);
        if(insert < 1) return Result.create(ResultEnum.INSERT_ERROR,null);

        int id = bookCategories.size() + 1;
        redisTemplate.opsForZSet().add("lib:TypeBookCount", "C" + id, 0);
        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + id, 0);


        return Result.create(ResultEnum.INSERT_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result deleteBookCategory(Integer id) {
        if(id == null) return Result.create(ResultEnum.DATA_IS_NULL,null);

        List<Book> books = bookMapper.selectAllByCategory(id);
        if(books.size() > 0) return Result.create(ResultEnum.BOOK_IN_CATEGORY_EXIST, null);

        //对于删除需要做的是：删除数据库记录的同时删除相关数据：缓存中对分类书籍的统计
        int i = bookCategoryMapper.deleteByPrimaryKey(id);
        if(i < 1) return Result.create(ResultEnum.DELETE_ERROR,null);

        redisTemplate.opsForZSet().remove("lib:TypeBookCount", "C" + id);
        redisTemplate.opsForHash().delete("lib:TypeBookCopyCount", "C" + id);
        return Result.create(ResultEnum.DELETE_SUCCESS,null);
    }

    @Transactional
    @Override
    public Result deleteBookCategories(Map<String, Object> map) {
        List<Integer> ids = (List<Integer>) map.get("ids");
        for(Integer id : ids){
            List<Book> books = bookMapper.selectAllByCategory(id);
            if(books.size() > 0) return Result.create(ResultEnum.BOOK_IN_CATEGORY_EXIST, null);

            //对于删除需要做的是：删除数据库记录的同时删除相关数据：缓存中对分类书籍的统计
            int i = bookCategoryMapper.deleteByPrimaryKey(id);
            redisTemplate.opsForZSet().remove("lib:TypeBookCount", "C" + id);
            redisTemplate.opsForHash().delete("lib:TypeBookCopyCount", "C" + id);
        }
        return Result.create(ResultEnum.DELETE_SUCCESS,null);
    }
}
