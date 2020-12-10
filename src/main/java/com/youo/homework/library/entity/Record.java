package com.youo.homework.library.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

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

    @NotEmpty
    @Size(min = 3, max = 14)
    private String pkUserName;

    @NotEmpty
    private String pkBookName;

    @NotEmpty
    private String author;

    private LocalDateTime borrowTime;

    private LocalDateTime returnTime;

    private Integer borrowState;


}
