package com.lib.service;

import com.lib.common.Result;

import java.awt.geom.RectangularShape;
import java.util.Map;

public interface BorrowReturnService {
    public Result addApprove(Map<String,Object> map);

    public Result returnBook(Map<String,Object> map);

    public Result deleteBorrowRecord(Map<String,Object> map);

    public Result batchDelete(Map<String,Object> map);
}
