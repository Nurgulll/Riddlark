package com.programming_distributed_systems_project;

import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class represents each reader in a team stored on the server
 * Basically tells java properties and methods availabe to a reader
 */
public class Reader implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ranking;
    private Character character;
    private int userId;
    private Socket connection;
    private  String name;

    public Reader (int userId, String name, Socket connection) {
        this.userId = userId;
        this.name = name;
        this.connection = connection;
    }

    public int getUserId() { return userId; }

    public String getName()  {return name;}
    public int getRanking() {
        return ranking;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public Character getCharacter() {
        return character;
    }

    public Socket getConnection() {
        return connection;
    }
    /**
     * Gives the reader a random ranking between 1 - 5
     */
    public void setRanking(int max, int min) {
        int index = ThreadLocalRandom.current().nextInt(min, max + 1);
        ranking = index;
    }
}
