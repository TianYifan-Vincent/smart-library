package com.lib.service;

import com.lib.common.Result;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

public interface LibHomePageService {
    public Result getHomePage(Integer id);

    public Result getBookInfo(Integer id);

    public Result searchBook(Integer pageNo, Integer pageSize, String keyWord, String category);

    public Result myRecord(Integer id, Integer pageNo, Integer pageSize, String keyWord);

    public Result getReaderInfo(Integer id);

    public Result editReaderInfo(Map<String, Object> map);
}
