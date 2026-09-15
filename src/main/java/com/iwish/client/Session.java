package com.iwish.client;

import java.util.Map;

public final class Session {
    private static int id;
    private static String name;
    private static String email;

    public static void set(Map<String,Object> user){id=((Number)user.get("id")).intValue();name=String.valueOf(user.get("name"));email=String.valueOf(user.get("email"));}
    public static int id(){return id;}
    public static String name(){return name;}
    public static String email(){return email;}
    public static void clear(){id=0;name=email=null;}
}
