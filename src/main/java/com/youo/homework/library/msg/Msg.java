package com.youo.homework.library.msg;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Msg {
    private Integer code;
    private String msg;
    private Map<String,Object> Info = new HashMap<>();

    public static Msg success(){
        Msg msg = new Msg();
        msg.setCode(100);
        msg.setMsg("处理成功");
        return msg;
    }

    public static Msg fail(){
        Msg msg = new Msg();
        msg.setCode(200);
        msg.setMsg("处理失败");
        return msg;
    }

    public Msg add(String info,Object o){
        this.getInfo().put(info,o);
        return this;
    }
}
