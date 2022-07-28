package com.lib.controller.admin;

import com.lib.common.Result;
import com.lib.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class StatisticsController {
/**
 * @ClassName StatisticsController
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/20 23:24
 * @Version 1.0
 **/

    @Autowired
    public StatisticsService statisticsService;

    @GetMapping("/statistics/Count")
    public Result getCount(){
        return statisticsService.getCount();
    }

}
