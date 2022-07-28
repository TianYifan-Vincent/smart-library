package com.lib.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReaderRecordVO {
/**
 * @ClassName ReaderRecordVO
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 3:19
 * @Version 1.0
 **/

    public Integer id;

    public String title;

    public String publisher;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date borrowTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date planTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    public Date returnTime;

    public Integer status;
}
