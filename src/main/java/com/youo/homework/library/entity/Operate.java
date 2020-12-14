package com.youo.homework.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
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
 * @since 2020-12-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Operate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "pk_op_id", type = IdType.AUTO)
    private Integer pkOpId;

    @NotEmpty
    @Size(min = 3, max = 14)
    private String opName;

    @NotEmpty
    private String opType;

    @NotEmpty
    @Size(min = 3, max = 14)
    private String opWho;

    private Integer opState;

    @NotEmpty
    private String opBook;

    private LocalDateTime opTime;


}
