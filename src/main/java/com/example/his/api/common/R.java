package com.example.his.api.common;

import org.apache.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {
    public R() {
        // Common property
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }

    /*
     * Override put method, add new property
     */
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "不明なエラーが発生しました。管理者に連絡してください。");
    }

}
