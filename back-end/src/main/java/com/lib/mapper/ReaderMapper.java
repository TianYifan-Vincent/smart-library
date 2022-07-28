package com.lib.mapper;

import com.lib.entity.Reader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReaderMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table reader
     *
     * @mbggenerated Tue Jul 05 13:25:15 CST 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table reader
     *
     * @mbggenerated Tue Jul 05 13:25:15 CST 2022
     */
    int insert(Reader record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table reader
     *
     * @mbggenerated Tue Jul 05 13:25:15 CST 2022
     */
    Reader selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table reader
     *
     * @mbggenerated Tue Jul 05 13:25:15 CST 2022
     */
    List<Reader> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table reader
     *
     * @mbggenerated Tue Jul 05 13:25:15 CST 2022
     */
    int updateByPrimaryKey(Reader record);

    List<Reader> selectReaderByEmail(@Param("email") String email);

    List<Reader> selectReaderByLoginName(@Param("loginName") String loginName);

    int updatePasswordAndSaltByEmail(@Param("email") String email,@Param("salt") String salt,@Param("password") String password);

    List<Reader> selectAllByKeyWord(@Param("keyWord") String keyWord);

    List<String> selectLoginNameById(@Param("id") Integer id);

    int editReader(@Param("id") Integer id, @Param("loginName") String loginName, @Param("readerName") String reader_name, @Param("gender") String gender, @Param("age") Integer age, @Param("phone") String phone);

    int updateReaderInfo(@Param("id") Integer id, @Param("salt") String salt, @Param("password") String password, @Param("gender") String gender, @Param("age") Integer age, @Param("email") String email, @Param("readerName") String readerName, @Param("phone") String phone);
}