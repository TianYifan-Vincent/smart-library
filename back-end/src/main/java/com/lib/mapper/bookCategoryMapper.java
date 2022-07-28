package com.lib.mapper;

import com.lib.entity.bookCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface bookCategoryMapper {
    List<bookCategory> selectAll();

    List<bookCategory> selectAllByKeyWord(@Param("keyWord") String keyWord);

    int updateNameById(@Param("categoryName") String categoryName,@Param("id") Integer id);

    int insert(bookCategory bookCategory);

    int deleteByPrimaryKey(Integer id);
}
