package com.lib.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class approve {
/**
 * @ClassName approve
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/15 10:19
 * @Version 1.0
 **/
    public int id;

    public int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date date;

    public int bookId;

    public int readerId;

    public int operation;
}
