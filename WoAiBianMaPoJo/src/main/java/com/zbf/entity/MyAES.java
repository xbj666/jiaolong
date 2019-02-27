package com.zbf.entity;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/18 16:17
 */
public enum MyAES {
    KEY("8a7cd3044f354684"),IV("8a7cd3044f354684");

    private String key;

    MyAES(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
