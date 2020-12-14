package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.Operate;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.OperateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lwy
 * @since 2020-12-10
 */
@RestController
@RequestMapping("/library/operate")
public class OperateController {

    @Autowired
    OperateServiceImpl operateService;

    @GetMapping("/getOpList/{page}/{size}")
    public Msg getOpList(@PathVariable("page") Integer page,
                         @PathVariable("size") Integer size){
        IPage<Operate> iPage = new Page<>(page,size);
        QueryWrapper<Operate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("op_time");
        IPage<Operate> operateIPage = operateService.page(iPage, queryWrapper);
        return Msg.success().add("OpInfo",operateIPage);
    }

    @PostMapping("/getOpListByOp/{page}/{size}")
    public Msg getOpList(@PathVariable("page") Integer page,
                         @PathVariable("size") Integer size,
                         @RequestBody Operate operate){
        IPage<Operate> iPage = new Page<>(page,size);
        QueryWrapper<Operate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(operate.getOpName()!=null,"op_name",operate.getOpName())
                .eq(operate.getOpType()!=null,"op_type",operate.getOpType())
                .eq(operate.getOpWho()!=null,"op_who",operate.getOpWho())
                .eq(operate.getOpState()!=null,"op_state",operate.getOpState())
                .eq(operate.getOpBook()!=null,"op_book",operate.getOpBook());
        IPage<Operate> operateIPage = operateService.page(iPage, queryWrapper);
        return Msg.success().add("OpInfo",operateIPage);
    }

    @GetMapping("/getOneOperate/{col}")
    public Msg getOneOperate(@PathVariable("col") String col){
        QueryWrapper<Operate> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(col)
                .groupBy(col);
        List<Object> objects = operateService.listObjs(queryWrapper);
        List<Map<String,Object>> maps = new ArrayList<>();
        for (Object object : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("text",object);
            map.put("value",object);
            maps.add(map);
        }
        return Msg.success().add("opCol",maps);
    }

}
