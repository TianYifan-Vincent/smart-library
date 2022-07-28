package com.lib.service;

import com.lib.common.Result;
import jdk.internal.loader.Resource;

public interface StatisticsService {

    public Result getReaderCount();

    public Result getWaitingApprovesCount();

    public Result getBookCount();

    public Result getCount();

    public Result getTypeBookRatio();

    public Result analyzeNewReader();

    public Result analyzeTypeBookCopyCount();
}
