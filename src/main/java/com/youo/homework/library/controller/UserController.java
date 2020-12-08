package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youo.homework.library.entity.User;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/library/user")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @PostMapping("/login")
    public Msg Login(@RequestBody User user){
        User one = selectByName(user.getPkName());
        if (one == null) {
            return Msg.fail().add("userInfo","用户名不存在");
        }
        if (!user.getPassword().equals(one.getPassword())){
            return Msg.fail().add("userInfo","密码错误");
        }
        return Msg.success().add("userInfo",one);
    }

    @PostMapping("/register")
    public Msg register(@RequestBody User user){
        String msg;
        if(null != selectByName(user.getPkName())){
            return Msg.fail().add("registerInfo","用户名已存在");
        }
        if(user.getUserLevel() == null){
            user.setUserLevel(0);
            msg = "注册";
        }else {
            msg = "添加";
        }
        boolean save = userService.save(user);
        if (save){
            return Msg.success().add("registerInfo",msg+"成功");
        }
        return Msg.fail().add("registerInfo",msg+"失败，请重试");
    }

    @GetMapping("/getUserByLevel/{level}/{page}/{size}")
    public Msg getUserByLevel(@PathVariable("level") Integer level,
                              @PathVariable("page") Integer page,
                              @PathVariable("size") Integer size){
        IPage<User> iPage = new Page<>(page,size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id,pk_name,password,user_level")
                .eq("user_level",level);
        IPage<User> levelPage = userService.page(iPage, queryWrapper);
        return Msg.success().add("UserInfo",levelPage);
    }

    @GetMapping("/getUserByLevel/{level}/{page}/{size}/{name}")
    public Msg searchUserByLevel(@PathVariable("level") Integer level,
                              @PathVariable("page") Integer page,
                              @PathVariable("size") Integer size,
                              @PathVariable("name") String name){
        IPage<User> iPage = new Page<>(page,size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id,pk_name,password,user_level")
                .eq("user_level",level)
                .like("pk_name",name);
        IPage<User> levelPage = userService.page(iPage, queryWrapper);
        return Msg.success().add("UserInfo",levelPage);
    }

    @DeleteMapping("/deleteUserById/{id}")
    public Msg deleteUser(@PathVariable("id") Integer id){
        boolean b = userService.removeById(id);
        if (b){
            return Msg.success().add("deleteInfo","删除成功");
        }
        return Msg.fail().add("deleteInfo","删除失败，请重试！");
    }

    @PutMapping("/updateUser")
    public Msg updateUser(@RequestBody User user){
        User selectUser = selectByName(user.getPkName());
        if (selectUser != null && !selectUser.getPkId().equals(user.getPkId())) {
            return Msg.fail().add("updateUserInfo","用户名已存在");
        }
        boolean update = userService.updateById(user);
        if (update){
            return Msg.success().add("updateUserInfo","更新成功");
        }
        return Msg.fail().add("updateUserInfo","更新失败，请重试");
    }

    public User selectByName(String userName){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id, pk_name, password, user_level")
                .eq("pk_name",userName);
        User one = userService.getOne(queryWrapper);
        return one;
    }
}
