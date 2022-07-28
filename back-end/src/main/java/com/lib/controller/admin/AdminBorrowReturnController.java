package com.lib.controller.admin;

import com.lib.common.Result;
import com.lib.service.AdminBorrowReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminBorrowReturnController {
/**
 * @ClassName AdminBorrowReturnController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/17 15:11
 * @Version 1.0
 **/

    @Autowired
    public AdminBorrowReturnService adminBorrowReturnService;

    @GetMapping("/waitingApproves/{pageNo}/{pageSize}")
    public Result getAllWaitingApproves(@PathVariable("pageNo") Integer pageNo,
                                 @PathVariable("pageSize") Integer pageSize){
        return adminBorrowReturnService.getAllWaitingApproves(pageNo, pageSize);
    }

    @GetMapping("/finishedApproves/{pageNo}/{pageSize}")
    public Result getAllFinishedApproves(@PathVariable("pageNo") Integer pageNo,
                                        @PathVariable("pageSize") Integer pageSize){
        return adminBorrowReturnService.getAllFinishedApproves(pageNo, pageSize);
    }

    @PutMapping("/waitingApproves/1")
    public Result allowWaitingApproves(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.allowWaitingApproves(map);
    }

    @PutMapping("/waitingApproves/all/1")
    public Result allAllowWaitingApproves(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.allAllowWaitingApproves(map);
    }

    @PutMapping("/waitingApproves/0")
    public Result rejectWaitingApproves(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.rejectWaitingApproves(map);
    }

    @PostMapping("/finishedApproves/delete")
    public Result deleteFinishedApproves(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.deleteFinishedApproves(map);
    }

    @PostMapping("/finishedApproves/deleteAll")
    public Result deleteAllFinishedApproves(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.deleteAllFinishedApproves(map);
    }

    /*---------------------------------------------------------------------*/
    @GetMapping("/borrowReturn/{pageNo}/{pageSize}/{keyWord}")
    public Result getAllBorrowReturnBooks(@PathVariable("pageNo") Integer pageNo,
                                          @PathVariable("pageSize") Integer pageSize,
                                          @PathVariable("keyWord") String keyWord){
        return adminBorrowReturnService.getAllBRB(pageNo, pageSize, keyWord);
    }

    @DeleteMapping("/borrowReturn/delete/{id}")
    public Result deleteBorrowReturnBooks(@PathVariable("id") Integer id){
        return adminBorrowReturnService.deleteBRB(id);
    }

    @PostMapping("/borrowReturn/deleteAll")
    public Result batchDeleteBorrowReturnBooks(@RequestBody Map<String,Object> map){
        return adminBorrowReturnService.batchDeleteBRBs(map);
    }
}
