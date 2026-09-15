package com.iwish.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        DatabaseManager db = new DatabaseManager();
        RequestHandler handler = new RequestHandler(db);
        int port = 5555;
        System.out.println("======================================");
        System.out.println("             i-Wish Server            ");
        System.out.println("======================================");
        System.out.println("Database connected successfully.");
        System.out.println("Server started on port " + port);
        System.out.println("Press Ctrl+C to stop.");

        ExecutorService pool=Executors.newCachedThreadPool();
        try(ServerSocket server=new ServerSocket(port)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running=false;
                pool.shutdownNow();
                System.out.println("Server stopped.");
            }));
            while(running) {
                Socket socket=server.accept();
                System.out.println("Client connected: "+socket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(socket,handler));
            }
        }
    }
}
