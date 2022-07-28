package com.lib.controller.admin;

import com.lib.common.Result;
import com.lib.common.ResultEnum;
import com.lib.entity.Book;
import com.lib.entity.borrowReturnBooks;
import com.lib.mapper.BookMapper;
import com.lib.mapper.borrowReturnBooksMapper;
import com.lib.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class BookController {
/**
 * @ClassName BookManage
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/15 14:17
 * @Version 1.0
 **/

    @Autowired
    public BookService bookService;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public borrowReturnBooksMapper borrowReturnBooksMapper;

    @GetMapping("/books/{pageNo}/{pageSize}/{keyWord}/{category}")
    public Result getAllBooks(@PathVariable("pageNo") Integer pageNo,
                              @PathVariable("pageSize") Integer pageSize,
                              @PathVariable("keyWord") String keyWord,
                              @PathVariable("category") String category){
        return bookService.getAllBooks(pageNo, pageSize, keyWord, category);
    }

    @PostMapping("/books/add")
    public Result addBook(@RequestBody Map<String, Object> map){
        return bookService.addBook(map);
    }

    @PutMapping("/books/edit")
    public Result editBook(@RequestBody Map<String, Object> map){
        return bookService.editBook(map);
    }

    @DeleteMapping("/books/delete/{id}/{category}")
    public Result deleteBook(@PathVariable("id") Integer id, @PathVariable("category") String category){
        return bookService.deleteBook(id, category);
    }

    @PostMapping("/books/delete")
    public Result deleteBooks(@RequestBody Map<String,Object> map){
        return bookService.deleteBooks(map);
    }


    @GetMapping("/bookCategory/{pageNo}/{pageSize}/{keyWord}")
    public Result getAllCategories(@PathVariable("pageNo") Integer pageNo,
                                   @PathVariable("pageSize") Integer pageSize,
                                   @PathVariable("keyWord") String keyWord){
        return bookService.getAllBookCategory(pageNo, pageSize, keyWord);
    }

    @PutMapping("/bookCategory/edit")
    public Result editBookCategory(@RequestBody Map<String,Object> map){
        return bookService.editBookCategory(map);
    }

    @PostMapping("/bookCategory/add")
    public Result addBookCategory(@RequestBody Map<String,Object> map){
        return bookService.addBookCategory(map);
    }


    @DeleteMapping("/bookCategory/delete/{id}")
    public Result deleteBookCategory(@PathVariable("id") Integer id){
        return bookService.deleteBookCategory(id);
    }

    @PostMapping("/bookCategory/delete")
    public Result deleteBookCategories(@RequestBody Map<String,Object> map){
        return bookService.deleteBookCategories(map);
    }






//    @GetMapping("/test")
//    public Result test(HttpServletRequest request){
//
////        List<Book> books = bookMapper.selectAllOrderByTime();
////        for (int i = 0; i < 5; i++){
////            redisTemplate.opsForZSet().add("lib:stNewBook", books.get(i).getId(), books.get(i).getCreatedTime().getTime());
////        }
////
////        redisTemplate.opsForZSet().add("lib:stNewBook", 100, new Date().getTime());
////        Set<Integer> set = redisTemplate.opsForZSet().reverseRange("lib:stNewBook", 0, -1);
////        for(Integer i : set) System.out.println(i);
////        System.out.println(request.getSession().getServletContext().getRealPath("/"));
//        int id = 11;
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 1, 500);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 2, 628);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 3, 529);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 4, 345);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 5, 458);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 6, 638);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 7, 127);
//        redisTemplate.opsForHash().put("lib:TypeBookCopyCount", "C" + 9, 19);
//
//        return Result.create(ResultEnum.QUERY_SUCCESS,null);
//
//
//    }
//
//
//
//
    @GetMapping("/redis")
    public Result redis(){
//        redisTemplate.opsForSet().remove("lib:WaitingMessagesList", "刘茂茂借阅《大话西游》审批中+2022-07-16 20:00:00");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "刘茂茂借阅《大话西游》审批中&2022-07-16 20:00:00");
//        Set<String> members = redisTemplate.opsForSet().members("lib:WaitingMessagesList");
//        for(String s : members) System.out.println(s);

//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112借阅《望江南》审批中&2019-01-16 15:57:23&1&15");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112借阅《房思琪的初恋乐园》审批中&2022-07-12 15:58:12&2&17");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112借阅《普林斯顿微积分读本（修订版）》审批中&2021-08-10 15:59:51&3&9");
//
//        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", "lml1112归还《房思琪的初恋乐园》审批中&2022-07-19 17:09:20&2&17");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112归还《房思琪的初恋乐园》审批中&2022-07-19 17:09:20&2&17");
//        redisTemplate.opsForSet().add("lib:FinishedMessagesList", "lml1112借阅《三体》审批中&2022-07-12 16:02:38&10&13&reject");
//
//
//
//        redisTemplate.opsForValue().set("lib:WaitingMessages",3);
//        redisTemplate.opsForValue().set("lib:FinishedMessages",2);
//        redisTemplate.opsForSet().remove("lib:WaitingMessagesList", "刘茂茂借阅《大话西游》审批中&2022-07-16 20:00:00");
//          Cursor scan = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match("*借阅*").build());
//          while(scan.hasNext()){
//              System.out.println(scan.next().toString());
//          }
//        Long add = redisTemplate.opsForSet().add("lib:WaitingMessagesList", -1);
//        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", "lml1112借阅《文城》审批中&2022-07-19 23:14:01&2&16");
//        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", "lml1112借阅《高等数学（第七版）（上册）》审批中&2022-07-19 23:12:45&2&6");
//        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", "lml1112借阅《文城》审批中&2022-07-19 23:14:01&2&16&reject");
//        redisTemplate.opsForSet().remove("lib:FinishedMessagesList", "lml1112借阅《高等数学（第七版）（上册）》审批中&2022-07-19 23:12:45&2&6&reject");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112借阅《文城》审批中&2022-07-19 23:14:01&2&16");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112借阅《高等数学（第七版）（上册）》审批中&2022-07-19 23:12:45&2&6");
//        redisTemplate.opsForSet().add("lib:WaitingMessagesList", "lml1112归还《望江南》审批中&2022-07-19 20:42:40&0&15");
//        redisTemplate.opsForSet().remove("lib:WaitingMessagesList","lml1112归还《发展心理学 第三版》审批中&2022-07-23 17:17:29&404&3");

//        String searchContent = "*" + "lml1112" + "*" + "2022-07-25 15:03:26" + "*";
//        Cursor scan1 = redisTemplate.opsForSet().scan("lib:WaitingMessagesList", ScanOptions.scanOptions().match(searchContent).build());
//        String str1 = new String();
//        str1 = scan1.next().toString();
        return Result.create(ResultEnum.SUCCESS,null);
    }

}
