package com.youo.homework;
import com.youo.homework.library.entity.Book;
import com.youo.homework.library.entity.Record;
import com.youo.homework.library.entity.User;
import com.youo.homework.library.service.impl.BookServiceImpl;
import com.youo.homework.library.service.impl.RecordServiceImpl;
import com.youo.homework.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SpringBootTest
class HomeworkApplicationTests {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private BookServiceImpl bookService;
    @Autowired
    private RecordServiceImpl recordService;

    @Test
    void testMybatisPlusForUser(){
        List<User> list = userService.list();
        System.out.println("list = " + list);
    }

    @Test
    void testBook(){
        int count = bookService.count();
        System.out.println("count = " + count);
        List<Book> list = bookService.list();
        System.out.println("list = " + list);
    }

    @Test
    void testRecord() {
        Record record = new Record();
        record.setPkBookId(1);record.setPkUserName("sys");record.setPkBookName("红楼梦");
        record.setAuthor("曹雪芹");record.setBorrowTime(LocalDateTime.now());record.setBorrowState(0);
        recordService.save(record);
        List<Record> list = recordService.list();
        System.out.println("list = " + list);
    }

    @Test
    void contextLoads() {
        System.out.println(LocalDateTime.now());
    }

}
