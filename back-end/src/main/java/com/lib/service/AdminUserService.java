package com.lib.service;

import com.lib.common.Result;

import java.util.Map;

public interface AdminUserService {

    Result adminUserRegistry(Map<String,Object> map);

    Result adminUserLogin(String loginName, String password);

    Result adminUserChangeCode(Map<String,Object> map);
}
