package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import java.io.*;
import java.net.Socket;

public class ApiClient {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ApiClient(String host,int port) throws Exception {
        socket=new Socket(host,port);
        out=new ObjectOutputStream(socket.getOutputStream());
        in=new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Response send(Request r) throws Exception {
        out.writeObject(r); out.flush();
        return (Response) in.readObject();
    }

    public void close(){try{socket.close();}catch(Exception ignored){}}
}
