package com.iwish.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Response implements Serializable {
    public boolean success;
    public String message;
    public Object data;
    public List<Map<String,Object>> rows = new ArrayList<>();

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public static Response ok(String m) { return new Response(true, m); }
    public static Response fail(String m) { return new Response(false, m); }
}
