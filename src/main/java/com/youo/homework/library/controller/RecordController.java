package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.Book;
import com.youo.homework.library.entity.Record;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.BookServiceImpl;
import com.youo.homework.library.service.impl.RecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lwy
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/library/record")
public class RecordController {

    @Autowired
    RecordServiceImpl recordService;
    @Autowired
    BookServiceImpl bookService;

    @GetMapping("/getRecordList/{page}/{sum}")
    public Msg getRecordList(@PathVariable("page") Integer page,
                             @PathVariable("sum") Integer sum){
        IPage<Record> iPage = new Page<>(page,sum);
        IPage<Record> recordIPage = recordService.page(iPage);
        return Msg.success().add("recordsInfo",recordIPage);
    }

    @PostMapping("/subscribe")
    public synchronized Msg subscribe(@RequestBody Record record){
        Record repeat = isRepeat(record.getPkBookId(), record.getPkUserName());
        if (repeat != null) {
            return Msg.fail().add("subscribe","您尚未归还本书，请勿重复借阅！");
        }
        record.setBorrowTime(LocalDateTime.now());
        record.setBorrowState(0);
        boolean save = recordService.save(record);
        if (save) {
            UpdateWrapper<Book> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("amount=amount-1")
                    .eq("pk_book_id",record.getPkBookId());
            boolean update = bookService.update(updateWrapper);
            if (update) {
                return Msg.success().add("subscribe","借阅成功");
            }
            return Msg.fail().add("subscribe","借阅失败，请重试!");
        }
        return Msg.fail().add("subscribe","借阅失败，请重试!");
    }

    public Record isRepeat(Integer bookId, String userName){
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_user_name, borrow_state")
                .eq("pk_book_id",bookId)
                .eq("pk_user_name",userName)
                .lt("borrow_state",3);
        return recordService.getOne(queryWrapper);
    }

}
