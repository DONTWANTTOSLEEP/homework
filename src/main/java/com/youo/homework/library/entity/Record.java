package com.youo.homework.library.entity;

import java.time.LocalDateTime;
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
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "pk_record_id", type = IdType.AUTO)
    private Integer pkRecordId;

    private Integer pkBookId;

    private String pkUserName;

    private String pkBookName;

    private String author;

    private LocalDateTime borrowTime;

    private LocalDateTime returnTime;

    private Integer borrowState;


}
