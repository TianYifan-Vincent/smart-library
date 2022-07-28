package com.lib.controller.admin;

import com.lib.common.Result;
import com.lib.service.AdminInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminInfoController {
/**
 * @ClassName AdminInfoController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/20 15:08
 * @Version 1.0
 **/
    @Autowired
    public AdminInfoService adminInfoService;

    @GetMapping("/info/{id}")
    public Result getAdminInfo(@PathVariable("id") Integer id){
        return adminInfoService.getAdminInfo(id);
    }

    @PutMapping("/info")
    public Result updateAdminInfo(@RequestBody Map<String, Object> map){
        return adminInfoService.updateAdminInfo(map);
    }

    @GetMapping("/getCount")
    public Result getWaitingApprovesCount(){
        return adminInfoService.getWaitingApproveCount();
    }

}
