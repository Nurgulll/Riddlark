package com.programming_distributed_systems_project;

import java.io.Serializable;
import java.net.Socket;

/**
 * This class represents each user in users stored on the server
 * Basically tells java properties and methods availabe to a user
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private int userId;
    private Integer teamId;
    public User (String username, String password, int userId) {
        this.username = username;
        this.password = password;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
    public String getPassword() {
        return password;
    }
    public String getUsername() {
        return username;
    }
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    public Integer getTeamId() { return this.teamId; }

    public static void main(String[] args) {
        User user = new User("famous", "famous", 1);
    }
}
