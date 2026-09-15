package com.iwish.server;

import com.iwish.common.Request;
import com.iwish.common.Response;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RequestHandler handler;

    public ClientHandler(Socket socket, RequestHandler handler){this.socket=socket;this.handler=handler;}

    @Override public void run() {
        try(socket;
            ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in=new ObjectInputStream(socket.getInputStream())) {
            while(!socket.isClosed()) {
                Object obj;
                try { obj=in.readObject(); } catch(EOFException e){break;}
                if(obj instanceof Request req) {
                    out.writeObject(handler.handle(req));
                    out.flush();
                }
            }
        } catch(Exception e) {
            System.out.println("Client disconnected: "+e.getMessage());
        }
    }
}
