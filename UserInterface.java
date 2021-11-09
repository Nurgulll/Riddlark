package com.programming_distributed_systems_project;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class handles all stuff which involves a user interface
 * This is used mainly as main menu and fallback in case the suddenly user leaves all application flow
 */
public class UserInterface {
    private static BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
    private Client client;
    private Socket connection;

    public UserInterface(Client client, Socket connection) {
        this.client = client;
        this.connection = connection;
    }

    /**
     * Interface shown when the user is about to choose a team
     * @param data
     */
    public void chooseTeamInterface(User user, Object data) {
        data = (ArrayList<Integer>) data;
        System.out.println("Choose a team from one of the teams below: ");
        for (int i = 1; i <= ((ArrayList) data).size(); i++) {
            System.out.println(i+". team"+ ((ArrayList) data).get(i - 1));
        }
        try {
            endInput: while(!Thread.interrupted()) {
                while(!scanner.ready()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break endInput;
                    }
                }
                    int teamSelection = new Integer(scanner.readLine());
                    if(teamSelection > ((ArrayList) data).size()) {
                        throw new NumberFormatException();
                    } else {
                        Request request = new Request((int) ((ArrayList) data).get(teamSelection - 1), user.getUserId(), "join team");
                        ClientOutputThread outputThread = new ClientOutputThread(connection, request);
                        Thread thread = new Thread(outputThread);
                        thread.start();
                        break;
                    }
            }
        } catch(NumberFormatException | IOException e) {
            printUnknownCommand();
        }
    }

    /**
     * Shows the user a list of results of all teams based on the previous gameplay
     */
    public void showResults(Object data) {
        System.out.println("The result of this game round is:");

    }

    /**
     * Shows the user an interface to pick a character
     * Gets the character picked by the user and sends as a choose character request to the server
     * @param data
     */
    public void chooseCharacterInterface(Object data){
        System.out.println("Choose a character from the list below: ");
        Script script = ((Script) data);
        ArrayList<Character> characters = script.getCharacters();

        for(int i = 1; i <= characters.size(); i++) {
            System.out.println(i + ". " + characters.get(i - 1));
        }
        try {
            endInput: while(!Thread.interrupted()) {
                while(!scanner.ready()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break endInput;
                    }
                }
                    int charSelection = new Integer(scanner.readLine());
                    if(charSelection >(characters.size())){
                        throw new NumberFormatException();
                    }else {
                        Request request = new Request(characters.get(charSelection - 1), "choose character");
                        ClientOutputThread outputThread = new ClientOutputThread(connection,request);
                        Thread thread = new Thread(outputThread);
                        thread.start();
                        break;
                    }
            }
        } catch (NumberFormatException | IOException e){
                printUnknownCommand();
        }
    }

    /**
     * This will print out different steps for a logged out user to follow
     * It will automatically load other classes and move users to new application flows
     */
    public void loggedOutInterface() {
        printExitInfo();
        System.out.println("What do you want to do? Enter number of command");
        System.out.println("1. Login");
        System.out.println("2. Register");
        Scanner scanner = new Scanner(System.in);

        outside: while(!Thread.interrupted()) {
            String input = scanner.next();
            if(isQuit(input)) {
                printThanks();
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    int command = new Integer(input);
                    switch (command) {
                        case 1:
                            client.login();
                            break outside;
                        case 2:
                            client.register();
                            break outside;
                        default:
                            throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    printUnknownCommand();
                }
            }
        }
    }

    /**
     * Checks if the user entered q to quit the application
     * @param command
     * @return true if the user entered q or false otherwise
     */
    public static boolean isQuit(String command) {
        return "exit".equals(command);
    }

    /**
     * Tell the user he entered a command which is not among the current valid list of commands
     */
    public static void printUnknownCommand() {
        System.out.println("That is not a valid command, please check command list");
    }

    /**
     * Show an info message on what the user should do if he wants to quit the application
     */
    public static void printExitInfo() {
        System.out.println("INFO: Enter `exit` to quit");
    }

    /**
     * Print a simple thanks message on terminating the application
     */
    public static void printThanks() {
        System.out.println("Thanks for playing...");
    }

    public static String newLine() {
        return System.getProperty("line.separator");//This will retrieve line separator dependent on OS.
    }

}
