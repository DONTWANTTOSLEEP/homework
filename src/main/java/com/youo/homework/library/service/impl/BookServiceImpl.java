package com.youo.homework.library.service.impl;

import com.youo.homework.library.entity.Book;
import com.youo.homework.library.mapper.BookMapper;
import com.youo.homework.library.service.IBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lwy
 * @since 2020-12-04
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements IBookService {

}
