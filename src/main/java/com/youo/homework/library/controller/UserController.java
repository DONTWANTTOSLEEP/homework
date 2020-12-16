package com.youo.homework.library.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.istack.internal.NotNull;
import com.youo.homework.library.entity.User;
import com.youo.homework.library.msg.Msg;
import com.youo.homework.library.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/library/user")
public class UserController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * function（登录）
     * 首先后端校验，然后在缓存中查询用户是否存在，密码是否正确
     * 否则进入数据库查询
     * @param user 用户对象
     * @param bindingResult 后端校验信息
     * @return Msg
     */
    @PostMapping("/login")
    public Msg Login(@RequestBody @Validated User user, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return Msg.fail().add("userInfo", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (redisTemplate.opsForHash().hasKey("UserList",user.getPkName())){
            logger.debug("从缓存读取用户信息...");
            User one = (User) redisTemplate.opsForHash().get("UserList", user.getPkName());
            assert one != null;
            if (one.getPassword().equals(user.getPassword())){
                return Msg.success().add("userInfo",one);
            } else {
                return Msg.fail().add("userInfo","密码错误");
            }
        } else {
            User one = selectByName(user.getPkName());
            if (one == null) {
                return Msg.fail().add("userInfo","用户名不存在");
            }
            if (!user.getPassword().equals(one.getPassword())){
                return Msg.fail().add("userInfo","密码错误");
            }
            redisTemplate.opsForHash().put("UserList",user.getPkName(),one);
            logger.debug("写用户信息进缓存...");
            return Msg.success().add("userInfo",one);
        }
    }

    /**
     * function（注册）
     * 首先后端校验，再根据userLevel是否为null判断是注册，还是管理员在添加用户
     * 首先判断缓存中是否存在用户名，不存在再移交数据库
     * 删除 缓存内容，为什么不增加？增加的信息不全
     * @param user 用户信息
     * @param bindingResult 后端校验信息
     * @return Msg
     */
    @PostMapping("/register")
    public synchronized Msg register(@RequestBody @Validated User user, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return Msg.fail().add("registerInfo",Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        String msg;
        if (redisTemplate.opsForHash().hasKey("UserList",user.getPkName())){
            return Msg.fail().add("registerInfo","用户名已存在");
        }
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
            deleteRedis();
            return Msg.success().add("registerInfo",msg+"成功");
        }
        return Msg.fail().add("registerInfo",msg+"失败，请重试");
    }

    /**
     * function（根据用户等级查询用户列表，用于后台管理界面）
     * 先查询缓存是否存在，否则才到数据库
     * @param level 用户等级
     * @param page 当前页
     * @param size 每页显示数据条数
     * @return 分页信息
     */
    @GetMapping("/getUserByLevel/{level}/{page}/{size}")
    public Msg getUserByLevel(@PathVariable("level") Integer level,
                              @PathVariable("page") Integer page,
                              @PathVariable("size") Integer size){
        if (redisTemplate.opsForHash().hasKey("Level"+level,page+"-"+size)){
            logger.debug("根据等级从缓存读取用户列表...");
            return Msg.success().add("UserInfo",redisTemplate.opsForHash().get("Level"+level,page+"-"+size));
        }
        IPage<User> iPage = new Page<>(page,size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id,pk_name,password,user_level")
                .eq("user_level",level);
        IPage<User> levelPage = userService.page(iPage, queryWrapper);
        redisTemplate.opsForHash().put("Level"+level,page+"-"+size,levelPage);
        return Msg.success().add("UserInfo",levelPage);
    }

    /**
     * function（在用户列表界面进行搜索，用户后台管理界面）
     * 先查询缓存是否存在，否则才到数据库
     * @param level 用户等级
     * @param page 当前页
     * @param size 每页大小
     * @param name 用户名
     * @return 分页信息
     */
    @GetMapping("/getUserByLevel/{level}/{page}/{size}/{name}")
    public Msg searchUserByLevel(@PathVariable("level") Integer level,
                              @PathVariable("page") Integer page,
                              @PathVariable("size") Integer size,
                              @PathVariable("name") String name){
        if (redisTemplate.opsForHash().hasKey("LevelSearch"+level,name+"-"+page+"-"+size)){
            logger.debug("从缓存搜索用户...");
            return Msg.success().add("UserInfo",redisTemplate.opsForHash().get("LevelSearch"+level,name+"-"+page+"-"+size));
        }
        IPage<User> iPage = new Page<>(page,size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id,pk_name,password,user_level")
                .eq("user_level",level)
                .like("pk_name",name);
        IPage<User> levelPage = userService.page(iPage, queryWrapper);
        redisTemplate.opsForHash().put("LevelSearch"+level,name+"-"+page+"-"+size,levelPage);
        return Msg.success().add("UserInfo",levelPage);
    }

    /**
     * function（删除用户）
     * 需清空缓存
     * @param id 用户id
     * @return Msg
     */
    @DeleteMapping("/deleteUserById/{id}")
    public Msg deleteUser(@PathVariable("id") Integer id){
        boolean b = userService.removeById(id);
        if (b){
            deleteRedis();
            return Msg.success().add("deleteInfo","删除成功");
        }
        return Msg.fail().add("deleteInfo","删除失败，请重试！");
    }

    /**
     * function（更新用户）
     * 首先后端校验，然后判断用户名是否已存在
     * 需清空缓存
     * @param user 用户信息
     * @param bindingResult 后端校验信息
     * @return Msg
     */
    @PutMapping("/updateUser")
    public synchronized Msg updateUser(@RequestBody @Validated User user, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return Msg.fail().add("updateUserInfo",Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        User selectUser = selectByName(user.getPkName());
        if (selectUser != null && !selectUser.getPkId().equals(user.getPkId())) {
            return Msg.fail().add("updateUserInfo","用户名已存在");
        }
        boolean update = userService.updateById(user);
        if (update){
            deleteRedis();
            return Msg.success().add("updateUserInfo","更新成功");
        }
        return Msg.fail().add("updateUserInfo","更新失败，请重试");
    }

    /**
     * function（通过用户名查询所有信息）
     * 首先判断缓存中是否存在，如果存在，直接返回
     * 否则进入数据库查询
     * @param userName 用户名
     * @return User
     */
    public User selectByName(String userName){
        if (redisTemplate.opsForHash().hasKey("UserList",userName)){
            return (User) redisTemplate.opsForHash().get("UserList",userName);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("pk_id, pk_name, password, user_level")
                .eq("pk_name",userName);
        return userService.getOne(queryWrapper);
    }

    /**
     * function（清空用户缓存）
     */
    public void deleteRedis(){
        redisTemplate.delete("UserList");
        redisTemplate.delete("Level0");
        redisTemplate.delete("Level1");
        redisTemplate.delete("LevelSearch0");
        redisTemplate.delete("LevelSearch1");
        logger.debug("清空用户缓存...");
    }
}
