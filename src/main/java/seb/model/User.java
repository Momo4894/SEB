package seb.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;

@JsonPOJOBuilder
public class User {
    private int id;
    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;
    @JsonProperty("Elo")
    private int elo;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Bio")
    private String bio;
    @JsonProperty("Image")
    private String image;
    private List<Integer> stats;

    public User() {
        this.elo = 100;
    }

    public User(int id, String username, String password) {
        this.id =id;
        this.username = username;
        this.password = password;
        this.elo = 100;
    }

    public User(int id, String username, String password, int elo, String name, String bio, String image) {
        this.id =id;
        this.username = username;
        this.password = password;
        this.elo = elo;
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    public User(String name, String bio, String image) {
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    public User(int id, String username, int elo) {
        this.id = id;
        this.username = username;
        this.elo = elo;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }

    public void setUsername(String username) { this.username = username; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void setName(String name) {
        this.name = name;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getName() { return name; }

    public String getBio() { return bio; }

    public int getElo() { return elo; }

    public String getImage() { return image; }
}
