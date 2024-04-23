package seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats {
    private int id;
    @JsonProperty("Name")
    private Exercise_Type name;
    @JsonProperty("DurationInSeconds")
    private int duration;
    @JsonProperty("Count")
    private int count;
    @JsonProperty("User_id")
    private int user_id;
    @JsonProperty("Tournament_id")
    private int tournament_id;

    public Stats(int id, int duration, int count, int user_id, int tournament_id) {
        this.id = id;
        this.duration = duration;
        this.count = count;
        this.user_id = user_id;
        this.tournament_id = tournament_id;
    }

    public Stats(int count, int user_id) {
        this.count = count;
        this.user_id = user_id;
    }

    public Stats(int duration, int count, int user_id, int tournament_id) {
        this.duration = duration;
        this.count = count;
        this.user_id = user_id;
        this.tournament_id = tournament_id;
    }

    //"{\"Name\": \"PushUps\",  \"Count\": 40, \"DurationInSeconds\": 60}"
    public Stats(Exercise_Type name, int count, int duration) {
        this.name = name;
        this.count = count;
        this.duration = duration;
    }

    public int getUser_id() { return user_id; }

    public int getTournament_id() { return tournament_id; }

    public int getCount() { return count; }

    public int getId() { return id; }

    public int getDuration() { return duration; }

    public Exercise_Type getName() { return name; }

    public void addToCount(int ammountToAdd) { count += ammountToAdd; }

    public void addIds(int userId, int tournamentId) {
        this.user_id = userId;
        this.tournament_id = tournamentId;
    }
}
