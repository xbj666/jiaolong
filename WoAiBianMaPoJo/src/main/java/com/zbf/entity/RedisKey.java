package com.zbf.entity;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/16 16:09
 */
public enum RedisKey {

    QUESTIONBANK("questionbank"),SHIJUANFENLEI("shijuanfenlei");

    private String key;

    RedisKey(String key){
        this.key=key;
    }

    public String getKey() {
        return key;
    }
}
