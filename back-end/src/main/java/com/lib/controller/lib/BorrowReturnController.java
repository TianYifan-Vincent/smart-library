package com.lib.controller.lib;

import com.lib.common.Result;
import com.lib.controller.vo.ReaderRecordVO;
import com.lib.service.BorrowReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/lib")
public class BorrowReturnController {
/**
 * @ClassName BorrowReturnController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 14:57
 * @Version 1.0
 **/

    @Autowired
    public BorrowReturnService borrowReturnService;

    @PutMapping("/borrow")
    public Result borrowBook(@RequestBody Map<String, Object> map){
        return borrowReturnService.addApprove(map);
    }

    @PutMapping("/return")
    public Result returnBook(@RequestBody Map<String, Object> map){
        return borrowReturnService.returnBook(map);
    }

    @PostMapping("/delete")
    public Result deleteRecord(@RequestBody Map<String, Object> map){
        return borrowReturnService.deleteBorrowRecord(map);
    }

    @PostMapping("/deletes")
    public Result batchDeleteRecord(@RequestBody Map<String, Object> map){
        return borrowReturnService.batchDelete(map);
    }
}
