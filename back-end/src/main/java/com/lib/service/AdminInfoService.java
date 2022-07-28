package com.lib.service;

import com.lib.common.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public interface AdminInfoService {
    public Result getAdminInfo(Integer id);
    public Result updateAdminInfo(Map<String, Object> map);
    public Result getWaitingApproveCount();
}
