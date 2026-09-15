package com.iwish.client;

import java.util.Map;

public final class Session {

    private static int id;
    private static String name;
    private static String email;

    private Session() {}

    public static void set(Map<String, Object> data) {
        Object idValue = data.get("id");

        if (idValue instanceof Number number) {
            id = number.intValue();
        } else if (idValue != null) {
            id = Integer.parseInt(idValue.toString());
        }

        name = data.get("name") == null ? "" : data.get("name").toString();
        email = data.get("email") == null ? "" : data.get("email").toString();
    }

    public static int id() {
        return id;
    }

    public static String name() {
        return name;
    }

    public static String email() {
        return email;
    }

    public static void clear() {
        id = 0;
        name = null;
        email = null;
    }
}