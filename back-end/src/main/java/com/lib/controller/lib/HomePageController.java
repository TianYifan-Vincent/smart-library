package com.lib.controller.lib;

import com.lib.common.Result;
import com.lib.service.LibHomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/lib")
public class HomePageController {
/**
 * @ClassName HomePageController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 1:46
 * @Version 1.0
 **/

    @Autowired
    public LibHomePageService libHomePageService;

    @GetMapping("/home/{id}")
    public Result getHomePage(@PathVariable("id") Integer id){
        return libHomePageService.getHomePage(id);
    }

    @GetMapping("/bookInfo/{id}")
    public Result getBookInfo(@PathVariable("id") Integer id){
        return libHomePageService.getBookInfo(id);
    }

    @GetMapping("/bookSearch/{pageNo}/{pageSize}/{keyWord}/{category}")
    public Result searchBook(@PathVariable("pageNo") Integer pageNo,
                             @PathVariable("pageSize") Integer pageSize,
                             @PathVariable("keyWord") String keyWord,
                             @PathVariable("category") String category){
        return libHomePageService.searchBook(pageNo,pageSize,keyWord,category);
    }

    @GetMapping("/myRecord/{id}/{pageNo}/{pageSize}/{keyWord}")
    public Result myRecord(@PathVariable("id") Integer id,
                             @PathVariable("pageNo") Integer pageNo,
                             @PathVariable("pageSize") Integer pageSize,
                             @PathVariable("keyWord") String keyWord){
        return libHomePageService.myRecord(id, pageNo, pageSize, keyWord);
    }

    @GetMapping("/getInfo/{id}")
    public Result getReaderInfo(@PathVariable("id") Integer id){
        return libHomePageService.getReaderInfo(id);
    }

    @PutMapping("/editInfo")
    public Result updateReaderInfo(@RequestBody Map<String, Object> map){
        return libHomePageService.editReaderInfo(map);
    }
}
