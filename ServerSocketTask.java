package com.programming_distributed_systems_project;

import java.io.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a single thread which will be given to each client connected to the server at a particular time
 * All request and replies made between the client and server are handled here on the server side
 */
public class ServerSocketTask implements Runnable{
    private static ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Team> teams = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Script> scripts = new ConcurrentHashMap<>();
    private Socket connection;  // Create Socket
    private ObjectInputStream clientRequest;
    private ObjectOutputStream serverReply;
    private User user;

    public ServerSocketTask(Socket s) {
        this.connection = s;
    }

    @Override
    public void run() {
            try {
                while(true) {
                    clientRequest = new ObjectInputStream(connection.getInputStream()); //Create a Request Buffer
                    Request request = (Request) clientRequest.readObject(); //Read Client request, Convert it to String
                    System.out.println("Client sent : " + request.toString()); //Print the client request
                    handleRequest(request);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("connection closed");
//                e.printStackTrace();
                this.killSocketTask();
            }
    }

    /**
     * Kill this socket task if the user disconnects
     */
    private void killSocketTask() {
        try {
            if(clientRequest != null) clientRequest.close();
            if(serverReply != null) serverReply.close();
            if(connection != null) connection.close();
        } catch (IOException e) {
            System.out.println("Couldn't kill server task");
//            e.printStackTrace();
        }

    }

    /**
     * Generates a random script for team
     * @param teamId
     */
    private Team addTeamScript(int teamId) {
        synchronized (teams) {
            Team team = teams.get(teamId);
            int teamRankingAverage = team.getTeamRankingAverage();
            Script script = new Script(teamRankingAverage);
            if(team.getScript() == null) {
                team.setScript(script);
            }
            return team;
        }
    }

    /**
     * Handles all request made from client to server
     * @param request
     * @throws IOException
     */
    private void handleRequest(Request request) throws IOException {
        String operation = request.getOperation();
        switch (operation) {
            case "register":
                this.register(request);
                break;
            case "login":
                this.login(request);
                break;
            case "join team":
                this.joinTeam(request);
                break;
            case "choose character":
                this.chooseCharacter(request);
                break;
        }
    }

    /**
     * This function can be used to perform server side register user functionality
     */
    private synchronized void register(Request request) throws IOException {
        int userId = users.size() + 1;
        String reqUsername = request.getUsername();
        String reqPassword = request.getPassword();
        boolean freeUserName = true;
        for(int i =  1; i <= users.size(); i++) {
            if(users.isEmpty() || request == null) {
                break;
            }
            User _user = users.get(i);
            String userName = _user.getUsername();
            if(userName.equals(reqUsername)) {
               freeUserName = false;
               break;
            }
        }
        if (freeUserName) {
            User user = new User(reqUsername, reqPassword, userId);
            users.put(userId, user);
            this.notifyClient("Successfully registered", null, null, "login", connection);
        } else {
            this.notifyClient("The username is taken", null, null, "retry", connection);
        }
    }

    /**
     * This function can be used to perform server side login user functionality
     */
    private synchronized void login(Request request) throws IOException {
        String reqPassword = request.getPassword();
        String reqUsername = request.getUsername();
        for(int i =  1; i <= users.size(); i++) {
            if(users.isEmpty() || request == null) {
                break;
            }
            User _user = users.get(i);
            String userPassword = _user.getPassword();
            String userName = _user.getUsername();
            if(userName.equals(reqUsername) && userPassword.equals(reqPassword)) {
                user = _user;
                break;
            }
        }
        if(user != null) {
            if(user.getTeamId() == null) {
                ArrayList<Integer> availableTeams = getAvailableTeams();
                if(availableTeams.size() < 1) {
                    Team newTeam = createTeam();
                    availableTeams.add(newTeam.getId());
                    this.addReaderToTeam(newTeam);
                    this.notifyClient("Successfully loggedIn" + UserInterface.newLine() + "You have been automatically added to team"+ newTeam.getId(), user, null, "wait", connection);
                    this.waitForTeamCompletion(newTeam);
                } else {
                    this.notifyClient("Successfully loggedIn", user, availableTeams,  "choose team", connection);
                }
            } else {
                this.notifyClient("Successfully loggedIn", user, null, "wait", connection);
                Team userTeam = teams.get(user.getTeamId());
                this.waitForTeamCompletion(userTeam);
            }
        } else {
            this.notifyClient("User details incorrect", null, null, "retry", connection);
        }
    }

    private void waitForTeamCompletion(Team team) throws IOException {
        synchronized (team) {
            try {
                team.wait();
                if(team.isFull()) {
                    this.notifyClient("Time to choose a character, you have 10 seconds",user, team.getScript(), "choose character",connection);
                    setChooseCharacterTimeout(team);
                }
            } catch (InterruptedException e) {
                System.out.println("team addition is interrupted is interrupted");
                e.printStackTrace();
            }
        }
    }

    private void setChooseCharacterTimeout(Team team) {
        setTimeout(() -> {
            synchronized (team) {
                if(team.getAssignedCharacters().size() != 3) {
                    assignCharacters(team);
                }
            }
        }, 10000);

    }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    /**
     * Creates a new team
     */
    private static Team createTeam() {
        synchronized (teams) {
            int numberOfTeams = teams.size();
            int teamId = numberOfTeams + 1;
            String newTeamName = "team" + teamId;
            Team newTeam = new Team(teamId, newTeamName);
            teams.put(teamId, newTeam);
            return newTeam;
        }
    }

    /**
     * Chooses a character for a user
     */
    private synchronized void chooseCharacter(Request request) throws IOException {
        Character chosenCharacter = request.getCharacter();
        int userId = user.getUserId();
        System.out.println(user.getTeamId());
        int teamId = user.getTeamId();
        Team team = teams.get(teamId);
        HashMap<Integer, Reader> readers = team.getReaders();
        Reader reader = team.getReader(userId);
        Character userCharacter = reader.getCharacter();
        if(userCharacter == null){
            ArrayList<Character> assignedCharacters = team.getAssignedCharacters();
            if(!assignedCharacters.contains(chosenCharacter)){
                reader.setCharacter(chosenCharacter);
                team.setAssignedCharacters(chosenCharacter);
                readers.forEach((k, v) -> { this.notifyClient(user.getUsername() + " chose " + chosenCharacter, user,null, "chosen character", v.getConnection());

                });
            }
            else {
                this.notifyClient(chosenCharacter + " has already been chosen", user, team.getScript(), "choose character", connection);
            }
        }
        else{
            this.notifyClient("You already have the character "+userCharacter,null,null,"chosen character", connection);
        }


    }


    /**
     * Assigns random characters to each user of a team if time finishes and user hasn't chosen a character yet
     * @param team team to assign characters to
     */
    private synchronized void assignCharacters(Team team) {
        HashMap<Integer, Reader> readers = team.getReaders();
        Script script = team.getScript();
        ArrayList<Character> characters = script.getCharacters();
        ArrayList<Character> assignedCharacters = team.getAssignedCharacters();

        if(assignedCharacters.size() != 3) {
            Reader reader = team.getReader(user.getUserId());
            Character userCharacter = reader.getCharacter();
            if (userCharacter == null) {
                for(char character : characters) {
                        if(!team.getAssignedCharacters().contains(character)) {
                            reader.setCharacter(character);
                            team.setAssignedCharacters(character);
                            readers.forEach((k, v) -> {
                                this.notifyClient(user.getUsername() + " chose " + reader.getCharacter() , user, null, "chosen character", v.getConnection());
                            });
                            break;
                        }
                    }
                }
        }
        // Print round results when the all users have characters
        if(assignedCharacters.size() == 3) {
            readers.forEach((k, v) -> {
                this.notifyClient("******************************************" + UserInterface.newLine() + "Here are the results of the game" + UserInterface.newLine() + "************************************" + UserInterface.newLine() + team.printRankingResults() + "*******************************************", null, null, "end game", v.getConnection());
            });
        }
    }

    /**
     * Adds a reader to a specific team
     * @param team
     * @return team which the user was added to
     */
    private Team addReaderToTeam(Team team) {
        Reader reader = new Reader(user.getUserId(), user.getUsername(), connection);
        user.setTeamId(team.getId());
        team.setReader(reader);
        return team;
    }

    /**
     * Adds a user to a team
     * @param request
     */
    private void joinTeam(Request request) throws IOException {
        int teamId = request.getTeamId();
        Team team = teams.get(teamId);
        if(!team.isFull()) {
            this.addReaderToTeam(team);
            if(team.isFull()) {
                synchronized (team) {
                    team = this.addTeamScript(team.getId());
                    this.notifyClient("Successfully joined team" + UserInterface.newLine() + "Time to choose a character", user, team.getScript(), "choose character", connection);
                    this.setChooseCharacterTimeout(team);
                    team.notifyAll();
                }
            } else {
                this.notifyClient("Successfully joined team", user, null, "wait", connection);
                this.waitForTeamCompletion(team);
            }
        } else {
            ArrayList<Integer> availableTeams = getAvailableTeams();
            if(availableTeams.size() < 1) {
                Team newTeam = createTeam();
                availableTeams.add(newTeam.getId());
                this.addReaderToTeam(newTeam);
                this.notifyClient("You have been  automatically added to team" + newTeam.getId(), user, null, "wait", connection);
            } else {
                this.notifyClient("Team"+ teamId + " unfortunately is full", user, availableTeams, "choose team", connection);
            }
        }
    }

    /**
     * Returns teams available for a user to join
     * @return
     */
    private ArrayList<Integer> getAvailableTeams() {
        ArrayList<Integer> availableTeams = new ArrayList<>();
        teams.forEach((k, v) -> {
            if(!v.isFull()) {
                availableTeams.add(v.getId());
            }
        });
        return availableTeams;
    }

    /**
     * Sends all replies from server to client
     * @param response
     */
    private void notifyClient(String response, User user, Object responseData, String nextOperation, Socket connection) {
        try {
            Reply reply = new Reply(response, user, responseData, nextOperation);
            serverReply = new ObjectOutputStream(connection.getOutputStream()); //Create a Reply Buffer
            serverReply.writeObject(reply); //write "Reply" in the outputStream
            serverReply.flush(); //Send written content to client
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
