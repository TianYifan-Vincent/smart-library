package com.lib.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookShowVO {
/**
 * @ClassName BookShowVO
 * @Description TODO
 * @Author 97569
 * @Date 2022/7/22 1:58
 * @Version 1.0
 **/
    public Integer id;

    public String url;

    public String title;

    public String author;

    public String isbn;

    public String publisher;
}
