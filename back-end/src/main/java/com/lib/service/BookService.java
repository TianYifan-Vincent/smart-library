package com.lib.service;

import com.lib.common.Result;

import java.util.Map;

public interface BookService {
    public Result getAllBooks(Integer pageNo, Integer pageSize, String keyWord, String category);
    public Result addBook(Map<String, Object> map);
    public Result editBook(Map<String, Object> map);
    public Result deleteBook(Integer id,String category);
    public Result deleteBooks(Map<String, Object> map);
    public Result getAllBookCategory(Integer pageNo, Integer pageSize, String keyWord);
    public Result editBookCategory(Map<String, Object> map);
    public Result addBookCategory(Map<String, Object> map);
    public Result deleteBookCategory(Integer id);
    public Result deleteBookCategories(Map<String, Object> map);

}
