package com.lib.controller.vo;

import com.lib.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBook
{
    private Integer id;


    private String title;


    private String isbn;


    private String coverImg;


    private String description;


    private String author;


    private BigDecimal price;


    private Integer bookCopyNumber;


    private String publisher;


    private String publishDate;


    private Integer userId;


    private Date createdTime;


    private String category;

}
