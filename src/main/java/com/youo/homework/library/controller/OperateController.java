package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.Operate;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.OperateServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private OperateServiceImpl operateService;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(OperateController.class);

    /**
     * function（获取所有的操作历史记录）
     * 先查看缓存，再到数据库
     * @param page 当前页
     * @param size 每页的大小
     * @return 分页信息
     */
    @GetMapping("/getOpList/{page}/{size}")
    public Msg getOpList(@PathVariable("page") Integer page,
                         @PathVariable("size") Integer size){
        if (redisTemplate.opsForHash().hasKey("OperateList",page+"-"+size)){
            logger.debug("从缓存获取操作记录...");
            return Msg.success().add("OpInfo",redisTemplate.opsForHash().get("OperateList",page+"-"+size));
        }
        IPage<Operate> iPage = new Page<>(page,size);
        QueryWrapper<Operate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("op_time");
        IPage<Operate> operateIPage = operateService.page(iPage, queryWrapper);
        redisTemplate.opsForHash().put("OperateList",page+"-"+size,operateIPage);
        return Msg.success().add("OpInfo",operateIPage);
    }

    /**
     * function（根据筛选条件，返回相应的数据）
     * @param page 当前页
     * @param size 每页的大小
     * @param operate 筛选的条件
     * @return 分页信息
     */
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

    /**
     * function（提供数据给筛选的下拉框）
     * @param col 需要哪一列的数据
     * @return 数据信息
     */
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
