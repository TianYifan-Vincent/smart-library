package com.lib.mapper;

import com.lib.entity.approve;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface approveMapper {
    List<approve> selectByReaderId(@Param("readerId") Integer readerId);

    List<approve> selectByBookId(@Param("bookId") Integer bookId);

    List<approve> selectAllWaitingMessages();

    List<approve> selectAllFinishedMessages();

    int updateStatusByBookIdAndReaderIdAndDateAndOperation(@Param("status") Integer status, @Param("bookId") Integer bookId, @Param("readerId") Integer readerId, @Param("operation") Integer operation, @Param("date") Date date);

    int deleteFinishedByParam(@Param("bookId") Integer bookId, @Param("readerId") Integer readerId, @Param("operation") Integer operation, @Param("date") Date date, @Param("status") Integer status);

    int insert(approve approve);

    int deleteWaitingByParam(@Param("bookId") Integer bookId, @Param("readerId") Integer readerId, @Param("operation") Integer operation, @Param("date") Date date, @Param("status") Integer status);
}
