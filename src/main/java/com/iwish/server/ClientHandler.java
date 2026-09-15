package com.iwish.server;

import java.io.EOFException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.iwish.common.Request;
import com.iwish.common.Response;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestHandler handler;

    public ClientHandler(Socket socket, RequestHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try (
                socket;
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {

            in.setObjectInputFilter(
                    ObjectInputFilter.Config.createFilter(
                            "com.iwish.common.Request;java.base/*;!*"
                    )
            );

            Integer authenticatedUserId = null;

            while (!socket.isClosed()) {

                Object obj;

                try {
                    obj = in.readObject();
                } catch (EOFException e) {
                    break;
                }

                if (!(obj instanceof Request request)) {
                    out.writeObject(Response.fail("Invalid request."));
                    out.flush();
                    continue;
                }

                if (request.action == null) {
                    out.writeObject(Response.fail("Missing action."));
                    out.flush();
                    continue;
                }

                if (!request.action.equals("LOGIN")
                        && !request.action.equals("REGISTER")) {

                    if (authenticatedUserId == null) {
                        out.writeObject(
                                Response.fail("Please sign in first.")
                        );
                        out.flush();
                        continue;
                    }

                    request.put("userId", authenticatedUserId);
                }

                Response response = handler.handle(request);

                if (request.action.equals("LOGIN")
                        && response.success
                        && response.data instanceof java.util.Map<?, ?> user) {

                    Object id = user.get("id");

                    if (id instanceof Number n) {
                        authenticatedUserId = n.intValue();
                    }
                }

                out.writeObject(response);
                out.flush();
            }

        } catch (Exception e) {
            System.out.println(
                    "Client disconnected: " + e.getMessage()
            );
        }
    }
}