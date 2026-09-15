package com.iwish.server;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.iwish.common.Request;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final RequestHandler handler;

    public ClientHandler(Socket socket,RequestHandler handler){
        this.socket=socket;
        this.handler=handler;
    }

    @Override
    public void run(){
        try(socket;
            ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in=new ObjectInputStream(socket.getInputStream())){
            while(!socket.isClosed()){
                Object obj;
                try{obj=in.readObject();}catch(EOFException e){break;}
                if(obj instanceof Request req){
                    out.writeObject(handler.handle(req));
                    out.flush();
                }
            }
        }catch(Exception e){
            System.out.println("Client disconnected: "+e.getMessage());
        }
    }
}