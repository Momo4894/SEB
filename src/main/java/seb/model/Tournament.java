package seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Tournament {

    private int id;
    @JsonProperty("Start_time")
    private Timestamp startTime;
    @JsonProperty("Status")
    private Status status;

    public Tournament(int id, Timestamp timestamp, Status status) {
        this.id = id;
        this.startTime = timestamp;
        this.status = status;
    }

    public int getId() { return id; }
}
