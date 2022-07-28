package com.lib.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.entity.borrowReturnBooks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class brbEntity {
/**
 * @ClassName brbEntity
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/19 23:11
 * @Version 1.0
 **/
    public Integer id;

    public String title;

    public String loginName;

    public String publisher;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date borrowTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date returnTime;

    public Integer status;
}
