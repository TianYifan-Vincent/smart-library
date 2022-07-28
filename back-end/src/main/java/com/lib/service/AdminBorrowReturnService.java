package com.lib.service;

import com.lib.common.Result;
import jdk.internal.loader.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface AdminBorrowReturnService {
    public Result getAllWaitingApproves(Integer pageNo,Integer pageSize);

    public Result getAllFinishedApproves(Integer pageNo,Integer pageSize);

    public Result allowWaitingApproves(Map<String,Object> map);

    public Result allAllowWaitingApproves(Map<String,Object> map);

    public Result rejectWaitingApproves(Map<String,Object> map);

    public Result deleteFinishedApproves(Map<String,Object> map);

    public Result deleteAllFinishedApproves(Map<String, Object> map);
    /*-------------------------------------------------------------------*/

    public Result getAllBRB(Integer pageNo, Integer pageSize, String keyWord);

    public Result deleteBRB(Integer id);

    public Result batchDeleteBRBs(Map<String,Object> map);
}
