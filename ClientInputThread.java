package com.programming_distributed_systems_project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientInputThread implements Runnable {
    private Socket connection;
    private ObjectInputStream inputStream;
    private UserInterface userInterface;
    private ExecutorService thPoolServer = Executors.newFixedThreadPool(5); //Create a pool of threads

    public ClientInputThread(Socket connection, UserInterface userInterface) {
        this.connection = connection;
        this.userInterface = userInterface;
    }

    @Override
    public void run() {
        try {
            handleReply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Handle all replies sent from server to client
     */
    public void handleReply() {
        endGame: while(!thPoolServer.isTerminated() && !thPoolServer.isShutdown()) {
            try {
                inputStream = new ObjectInputStream(connection.getInputStream());
                Reply reply = (Reply) inputStream.readObject(); //Read Server Reply
                String nextOperation = reply.nextOperation();
                Object replyData = reply.getReplyData();
                System.out.println(reply.getResponse());
                switch (nextOperation) {
                    case "choose team": {
                        User user = reply.getUser();
                        thPoolServer.execute(() -> userInterface.chooseTeamInterface(user, replyData));
                        break;
                    }
                    case "wait": {
                        System.out.println("Wait for the team to get full");
                        this.handleReply();
                        break;
                    }
                    case "retry": {
                        thPoolServer.execute(() -> userInterface.loggedOutInterface());
                        break;
                    }
                    case "login": {
                        System.out.println("You are now successfully registered, login to activate your account");
                        thPoolServer.execute(() -> userInterface.loggedOutInterface());
                        break;
                    }
                    case "choose character": {
                        thPoolServer.execute(() -> userInterface.chooseCharacterInterface(replyData));
                        break;
                    }
                    case "chosen character": {
                        Team team = (Team) reply.getReplyData();
    //                    System.out.println(team.printCharacterSelection());
                        break;
                    }
                    case "end game": {
                        thPoolServer.shutdownNow();
                        break endGame;
                    }
                }
            } catch(ClassNotFoundException | IOException e ) {
                System.out.println("Lost connection to server, terminating...");
//                e.printStackTrace();
                break;
            }
        }
    }

}
