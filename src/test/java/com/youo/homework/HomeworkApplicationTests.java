package com.youo.homework;
import ch.qos.logback.classic.LoggerContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.youo.homework.library.entity.Book;
import com.youo.homework.library.entity.Record;
import com.youo.homework.library.entity.User;
import com.youo.homework.library.service.impl.BookServiceImpl;
import com.youo.homework.library.service.impl.RecordServiceImpl;
import com.youo.homework.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class HomeworkApplicationTests {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private BookServiceImpl bookService;
    @Autowired
    private RecordServiceImpl recordService;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Test
    void testRedis(){
        //book
        List<Book> list = bookService.list();
        System.out.println("list = " + list);
        //添加list
        redisTemplate.opsForList().leftPushAll("Books", list);
    }

    @Test
    void testLogback(){
        Logger logger = LoggerFactory.getLogger("com.youo.homework.HomeworkApplicationTests.testLogback");
//        logger.debug("test debug");
        logger.error("test error");
        LoggerContext iLoggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
        System.out.println("iLoggerFactory = " + iLoggerFactory);
    }

    @Test
    void testMybatisPlusForUser(){
        List<User> list = userService.list();
        System.out.println("list = " + list);
    }

    @Test
    void testBook(){
        boolean b = bookService.removeById(252);
        System.out.println("b = " + b);
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
    void addUser(){
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 111; i++) {
            User user = new User();
            user.setPkName("User"+i);
            user.setPassword("11111"+i);
            user.setUserLevel(0);
            users.add(user);
        }
        userService.saveBatch(users);
    }

    @Test
    void selectUser(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id,pk_name,password,user_level")
                .eq("user_level",0);
        List<User> list = userService.list(queryWrapper);
        System.out.println("list = " + list);
    }

    @Test
    void contextLoads() {
        System.out.println(LocalDateTime.now());
    }

}
