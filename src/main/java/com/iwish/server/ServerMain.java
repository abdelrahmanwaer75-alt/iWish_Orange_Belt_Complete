package com.iwish.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain{
    public static void main(String[] args)throws Exception{
        DatabaseManager db=new DatabaseManager();
        RequestHandler handler=new RequestHandler(db);
        ExecutorService pool=Executors.newCachedThreadPool();

        try(ServerSocket server=new ServerSocket(5555)){
            System.out.println("i-Wish Server started on port 5555");
            while(true){
                Socket socket=server.accept();
                pool.submit(new ClientHandler(socket,handler));
            }
        }finally{
            pool.shutdown();
        }
    }
}