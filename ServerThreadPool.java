package com.programming_distributed_systems_project;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains the pool of all threads present on the server at a particular time
 * It starts each client on a new thread and executes its own task (see ServerSocketTask)
 */
public class ServerThreadPool {

    private static Socket connection; //Create Socket
    private static ServerSocket serverSocket; //Create a Server Socket
    private static ExecutorService thPoolServer = Executors.newFixedThreadPool(15); //Create a pool of threads
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(1235); //Start a new server socket on port 1234
        while (true) {
            connection = serverSocket.accept();//Accept when a request arrives
            ServerSocketTask serverTask = new ServerSocketTask(connection);//Start a task Thread to handle client request
            thPoolServer.execute(serverTask);//Execute Thread
        }
    }
}
