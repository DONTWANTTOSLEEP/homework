package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.Book;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.BookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/getBookList/{page}/{size}")
    public Msg getBookList(@PathVariable("page") Integer page,
                           @PathVariable("size") Integer size){
        IPage<Book> pageInfo = new Page<>(page,size);
        IPage<Book> getBookPage = bookService.page(pageInfo);
        return Msg.success().add("BookList",getBookPage);
    }

    @GetMapping("/getBookList/{page}/{size}/{name}")
    public Msg searchBookList(@PathVariable("page") Integer page,
                              @PathVariable("size") Integer size,
                              @PathVariable("name") String name){
        IPage<Book> pageInfo = new Page<>(page,size);
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_book_name, author, amount")
                .like("pk_book_name",name);
        IPage<Book> searchBookPage = bookService.page(pageInfo, queryWrapper);
        return Msg.success().add("BookList",searchBookPage);
    }

    @PostMapping("/addBook")
    public synchronized Msg addBook(@RequestBody Book book){
        Book bookByName = getBookByName(book.getPkBookName());
        if (bookByName != null) {
            return Msg.fail().add("addBookInfo","书已存在");
        }
        boolean save = bookService.save(book);
        if (save) {
            return Msg.success().add("addBookInfo","添加成功");
        }
        return Msg.fail().add("addBookInfo","添加失败，请重试！");
    }

    @DeleteMapping("/deleteBookById/{id}")
    public Msg deleteBook(@PathVariable("id") Integer id){
        boolean b = bookService.removeById(id);
        if (b){
            return Msg.success().add("deleteInfo","删除成功");
        }
        return Msg.fail().add("deleteInfo","删除失败，请重试！");
    }

    @PutMapping("/updateBook")
    public synchronized Msg updateBook(@RequestBody Book book){
        Book bookByName = getBookByName(book.getPkBookName());
        if (bookByName != null && !bookByName.getPkBookId().equals(book.getPkBookId())) {
            return Msg.fail().add("updateBookInfo","本书已存在");
        }
        boolean b = bookService.updateById(book);
        if (b) {
            return Msg.success().add("updateBookInfo","更新成功");
        }
        return Msg.fail().add("updateBookInfo","更新失败，请重试！");
    }

    public Book getBookByName(String name){
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_book_id, pk_book_name, author, amount")
                .eq("pk_book_name", name);
        return bookService.getOne(queryWrapper);
    }
}
