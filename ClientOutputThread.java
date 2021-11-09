package com.programming_distributed_systems_project;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientOutputThread implements Runnable {
    private Socket connection;
    private ObjectOutputStream outputStream;
    private Request request;

    public ClientOutputThread(Socket connection, Request request) {
        this.connection = connection;
        this.request = request;
    }

    @Override
    public void run() {
            try {
                sendRequest(request);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

    }

    /**
     * Handle all request made by client to server
     * @param request
     */
    public void sendRequest(Request request) {
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>");
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Couldn't connect to the server...");
            e.printStackTrace();
        }
    }

}
