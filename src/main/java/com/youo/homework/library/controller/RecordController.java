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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @GetMapping("/getRecordByAdmin/{page}/{sum}")
    public Msg getRecordByAdmin(@PathVariable("page") Integer page,
                                @PathVariable("sum") Integer sum){
        IPage<Record> iPage = new Page<>(page,sum);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_user_name, pk_book_name, author, borrow_time, borrow_state")
                .in("borrow_state",0,2)
                .orderByDesc("borrow_time");
        IPage<Record> recordIPage = recordService.page(iPage,queryWrapper);
        return Msg.success().add("recordsInfo",recordIPage);
    }

    @GetMapping("/getRecordListByUser/{page}/{sum}/{borrowState}/{userName}")
    public Msg getRecordListByUser(@PathVariable("page") Integer page,
                             @PathVariable("sum") Integer sum,
                             @PathVariable("borrowState") Integer state,
                             @PathVariable("userName") String userName){
        int var1 = -1,var2 = 1;
        if (state.equals(1)) {
            var1 = 2;var2=3;
        }
        IPage<Record> iPage = new Page<>(page,sum);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_user_name, pk_book_name, author, borrow_time, return_time, borrow_state")
                .eq("pk_user_name",userName)
                .between("borrow_state",var1,var2)
                .orderByDesc("borrow_time");
        IPage<Record> recordIPage = recordService.page(iPage,queryWrapper);
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
            return Msg.success().add("subscribe","借阅成功，请等待管理员同意");
        }
        return Msg.fail().add("subscribe","借阅失败，请重试!");
    }

    @Transactional
    @PutMapping("/updateRecord")
    public synchronized Msg agree(@RequestBody Record record){
        System.out.println("record = " + record);
        if (record.getBorrowState().equals(0)){
            UpdateWrapper<Record> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("borrow_state", 1)
                    .eq("pk_book_id", record.getPkBookId())
                    .eq("pk_user_name", record.getPkUserName())
                    .eq("borrow_state", 0);
            boolean agreeBorrow = recordService.update(updateWrapper);
            if (agreeBorrow) {
                UpdateWrapper<Book> bookUpdateWrapper = new UpdateWrapper<>();
                bookUpdateWrapper.setSql("amount=amount-1")
                        .eq("pk_book_id",record.getPkBookId());
                boolean bookUpdate = bookService.update(bookUpdateWrapper);
                if (bookUpdate) {
                    return Msg.success().add("agreeInfo","借阅申请已同意");
                }
                return Msg.fail().add("agreeInfo","操作失败，请重试");
            }
            return Msg.fail().add("agreeInfo","操作失败，请重试");
        }

        UpdateWrapper<Record> wrapper = new UpdateWrapper<>();
        wrapper.set("borrow_state", 3)
                .set("return_time", LocalDateTime.now())
                .eq("pk_book_id", record.getPkBookId())
                .eq("pk_user_name", record.getPkUserName())
                .eq("borrow_state", 2);
        Object savepoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        boolean agree = recordService.update(wrapper);
        if (agree) {
            UpdateWrapper<Book> updateWrapperBook = new UpdateWrapper<>();
            updateWrapperBook.setSql("amount=amount+1")
                    .eq("pk_book_id",record.getPkBookId());
            boolean bookUpdate = bookService.update(updateWrapperBook);
            if (bookUpdate) {
                return Msg.success().add("agreeInfo","归还申请已同意");
            }
            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savepoint);
            return Msg.fail().add("agreeInfo","操作失败，请重试");
        }
        return Msg.fail().add("agreeInfo","操作失败，请重试");
    }

    @PutMapping("/refuse")
    public Msg refuse(@RequestBody Record record){
        UpdateWrapper<Record> recordUpdateWrapper = new UpdateWrapper<>();
        recordUpdateWrapper.set("borrow_state", -1)
                .eq("pk_book_id", record.getPkBookId())
                .eq("pk_user_name", record.getPkUserName())
                .eq("borrow_state", 0);
        boolean agreeBorrow = recordService.update(recordUpdateWrapper);
        if (agreeBorrow) {
            return Msg.success().add("refuseInfo","借阅申请已拒绝");
        }
        return Msg.fail().add("refuseInfo","操作失败，请重试");
    }

    @PutMapping("/returnBook")
    public Msg returnBook(@RequestBody Record record){
        UpdateWrapper<Record> UpdateWrapperRecord = new UpdateWrapper<>();
        UpdateWrapperRecord.set("borrow_state", 2)
                .eq("pk_book_id", record.getPkBookId())
                .eq("pk_user_name", record.getPkUserName())
                .eq("borrow_state", 1);
        boolean agreeBorrow = recordService.update(UpdateWrapperRecord);
        if (agreeBorrow) {
            return Msg.success().add("returnInfo","已提交归还申请");
        }
        return Msg.fail().add("returnInfo","操作失败，请重试");
    }

    public Record isRepeat(Integer bookId, String userName){
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_user_name, borrow_state")
                .eq("pk_book_id",bookId)
                .eq("pk_user_name",userName)
                .between("borrow_state", 0, 2);
        return recordService.getOne(queryWrapper);
    }

}
