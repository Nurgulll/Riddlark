package com.programming_distributed_systems_project;

import java.io.IOException;
import java.net.Socket;

public class Main {
    /**
     * Entry point of client side of application
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Welcome To Team Maker System");
        Socket connection = connect();
        if(connection != null) {
            Client client = new Client(connection);
            UserInterface userInterface = new UserInterface(client, connection);
            System.out.println("Connected to server");
            Thread clientInputThread = new Thread(new ClientInputThread(connection, userInterface));
            clientInputThread.start();
            userInterface.loggedOutInterface();
        } else {
            System.out.println("You have no connection to the server. Please start server before launching client");
        }
    }
    /**
     * Connects the client to the server
     */
    private static Socket connect() {
        int port = 1235; // initialize port number
        String ip = "localhost"; // localhost ip address = 127.0.0.1
        try {
            Socket connection = new Socket(ip, port); //Create a Client Socket for "localhost" address and port
            return connection;
        } catch(IOException e) {
//            e.printStackTrace();
            return null;
        }
    }
}
