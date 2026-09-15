package com.iwish.server;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        DatabaseManager db = new DatabaseManager();
        RequestHandler handler = new RequestHandler(db);

        Properties properties = new Properties();

        try (InputStream in = ServerMain.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }

        int port = Integer.parseInt(
                System.getenv().getOrDefault(
                        "IWISH_SERVER_PORT",
                        properties.getProperty("server.port", "5555")
                )
        );

        String host = System.getenv().getOrDefault(
                "IWISH_SERVER_HOST",
                properties.getProperty("server.host", "127.0.0.1")
        );

        ExecutorService pool = Executors.newCachedThreadPool();

        try (
                ServerSocket server = new ServerSocket(
                        port,
                        50,
                        InetAddress.getByName(host)
                )
        ) {

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        pool.shutdownNow();
                        System.out.println("i-Wish Server stopped.");
                    })
            );

            System.out.println(
                    "i-Wish Server started on " + host + ":" + port
            );

            System.out.println("Press Ctrl+C to stop safely.");

            while (!server.isClosed()) {

                Socket socket = server.accept();

                socket.setSoTimeout(120000);

                pool.submit(
                        new ClientHandler(socket, handler)
                );
            }

        } finally {
            pool.shutdownNow();
        }
    }
}