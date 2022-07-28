package com.lib.service;

import com.lib.common.Result;

import java.util.List;
import java.util.Map;

public interface AdminReaderService {
    Result getAllReaders(Integer pageNo, Integer pageSize, String keyWord);

    Result editReader(Map<String, Object> map);

    Result addReader(Map<String, Object> map);

    Result deleteReader(Integer id);

    Result deleteReaders(Map<String ,Object> map);
}
