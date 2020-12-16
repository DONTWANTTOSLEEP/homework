package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.Book;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.BookServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lwy
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/library/book")
public class BookController {
    @Autowired
    private BookServiceImpl bookService;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);


    /**
     * function(获取当前页的书籍列表)
     * 判断 Redis 中是否存在，如果存在直接读取 Redis中的数据
     * key 为 BookList<page+pageNum,pageInfo>
     * @param page 当前页
     * @param size 每页显示多少条数据
     * @return 分页信息
     */
    @GetMapping("/getBookList/{page}/{size}")
    public Msg getBookList(@PathVariable("page") Integer page,
                           @PathVariable("size") Integer size) {
        if (redisTemplate.opsForHash().hasKey("BookList",page+"-"+size)){
            logger.debug("获取图书列表，从缓存读取...");
            return Msg.success().add("BookList",redisTemplate.opsForHash().get("BookList",page+"-"+size));
        }else {
            IPage<Book> pageInfo = new Page<>(page,size);
            IPage<Book> getBookPage = bookService.page(pageInfo);
            redisTemplate.opsForHash().put("BookList",page+"-"+size,getBookPage);
            logger.debug("获取图书列表，写入缓存...");
            return Msg.success().add("BookList",getBookPage);
        }
    }

    /**
     * function（搜索书籍）
     * 判断缓存中是否有人搜索过相同的关键字，有直接返回，负责链接数据库查询
     * key 为 BookSearch<name,bookList>
     * @param page 首页
     * @param size 每页显示数目
     * @param name 需要搜索的书名
     * @return 书籍信息
     */
    @GetMapping("/getBookList/{page}/{size}/{name}")
    public Msg searchBookList(@PathVariable("page") Integer page,
                              @PathVariable("size") Integer size,
                              @PathVariable("name") String name){
        if (redisTemplate.opsForHash().hasKey("BookSearch", name+"-"+page+"-"+size)){
            logger.debug("搜索图书列表，从缓存读取...");
            return Msg.success().add("BookList",redisTemplate.opsForHash().get("BookSearch",name+"-"+page+"-"+size));
        } else {
            IPage<Book> pageInfo = new Page<>(page,size);
            QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("pk_book_id, pk_book_name, author, amount")
                    .like("pk_book_name",name);
            IPage<Book> searchBookPage = bookService.page(pageInfo, queryWrapper);
            logger.debug("搜索图书列表，写入缓存...");
            redisTemplate.opsForHash().put("BookSearch",name+"-"+page+"-"+size,searchBookPage);
            return Msg.success().add("BookList",searchBookPage);
        }
    }

    /**
     * function（添加图书）
     * 首先判断后端校验是否通过
     * 如果通过了--需要删除 Redis 的缓存数据
     * @param book 一个书籍对象
     * @param bindingResult 后端校验信息
     * @return 操作信息
     */
    @PostMapping("/addBook")
    public synchronized Msg addBook(@RequestBody @Validated Book book, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return Msg.fail().add("addBookInfo", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        Book bookByName = getBookByName(book.getPkBookName());
        if (bookByName != null) {
            return Msg.fail().add("addBookInfo","书已存在");
        }
        boolean save = bookService.save(book);
        if (save) {
            deleteRedis();
            return Msg.success().add("addBookInfo","添加成功");
        }
        return Msg.fail().add("addBookInfo","添加失败，请重试！");
    }

    /**
     * function（删除图书）
     * 删除成功后需要清楚图书相关缓存
     * @param id 图书id
     * @return Msg
     */
    @DeleteMapping("/deleteBookById/{id}")
    public Msg deleteBook(@PathVariable("id") Integer id){
        boolean b = bookService.removeById(id);
        if (b){
            deleteRedis();
            return Msg.success().add("deleteInfo","删除成功");
        }
        return Msg.fail().add("deleteInfo","删除失败，请重试！");
    }

    /**
     * function（更新图书信息）
     * 更新成功后也要删除图书相关缓存
     * 首先后端校验，再判断更新后的书籍名称是否与其他存在的书籍名重复
     * @param book 一个书籍对象
     * @param bindingResult 后端校验信息
     * @return Msg
     */
    @PutMapping("/updateBook")
    public synchronized Msg updateBook(@RequestBody @Validated Book book,BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return Msg.fail().add("updateBookInfo",Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        Book bookByName = getBookByName(book.getPkBookName());
        if (bookByName != null && !bookByName.getPkBookId().equals(book.getPkBookId())) {
            return Msg.fail().add("updateBookInfo","本书已存在");
        }
        boolean b = bookService.updateById(book);
        if (b) {
            deleteRedis();
            return Msg.success().add("updateBookInfo","更新成功");
        }
        return Msg.fail().add("updateBookInfo","更新失败，请重试！");
    }

    /**
     * function（根据书名查询图书信息）
     * @param name 书名
     * @return 图书信息
     */
    public Book getBookByName(String name){
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_book_name, author, amount")
                .eq("pk_book_name", name);
        return bookService.getOne(queryWrapper);
    }

    /**
     * function（删除图书的相关缓存）
     */
    public void deleteRedis() {
        redisTemplate.delete("BookList");
        redisTemplate.delete("BookSearch");
        logger.debug("删除图书缓存...");
    }
}
