package com.lib.controller.admin;

import com.lib.common.Result;
import com.lib.service.AdminReaderService;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminReaderController {
/**
 * @ClassName AdminReaderController
 * 管理员管理读者接口
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/10 13:53
 * @Version 1.0
 **/

    @Autowired
    public AdminReaderService adminReaderService;

    @GetMapping("/readers/{pageNo}/{pageSize}/{keyWord}")
    public Result getAllReaders(@PathVariable("pageNo") Integer pageNo,
                                @PathVariable("pageSize") Integer pageSize,
                                @PathVariable("keyWord") String keyWord)
    {
        return adminReaderService.getAllReaders(pageNo, pageSize, keyWord);
    }

    @PutMapping("/readers/edit")
    public Result editReader(@RequestBody Map<String, Object> map){
        return adminReaderService.editReader(map);
    }

    @PostMapping("/readers/add")
    public Result addReader(@RequestBody Map<String, Object> map){
        return adminReaderService.addReader(map);
    }

    @DeleteMapping("/readers/delete/{id}")
    public Result deleteReaders(@PathVariable("id") Integer id){
        return adminReaderService.deleteReader(id);
    }

    @PostMapping("/readers/delete")
    public Result deleteReaders(@RequestBody Map<String,Object> map){
        return adminReaderService.deleteReaders(map);
    }

}
