package com.youo.homework.library.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author lwy
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "pk_book_id", type = IdType.AUTO)
    private Integer pkBookId;

    private String pkBookName;

    private String author;

    private Integer amount;


}
