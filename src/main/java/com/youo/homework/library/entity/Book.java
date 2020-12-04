package com.youo.homework.library.entity;

import java.io.Serializable;
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

    private Integer pkBookId;

    private String pkBookName;

    private String author;

    private Integer amount;


}
