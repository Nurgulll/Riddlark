package com.programming_distributed_systems_project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class describes how a theater script should look like
 */
public class Script implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Character> characters = new ArrayList<>();
    private int difficulty;

    public Script (int difficulty) {
        this.difficulty = difficulty;
        generateCharacters();
    }

    public int getDifficulty() {
        return difficulty;
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    /**
     * Adds random characters to script
     */
    public void generateCharacters() {
        for(int i = 0; i < 3; i++) {
            this.characters.add(getRandomCharacter());
        }
    }

    /**
     * Returns a random character between [a-z]
     * @return random character
     */
    private char getRandomCharacter() {
        Random r = new Random();
        char c = (char)(r.nextInt(26) + 'A');
        if(characters.contains(c)) {
            return getRandomCharacter();
        } else {
            return c;
        }
    }
}
