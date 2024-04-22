package seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats {
    private int id;
    @JsonProperty("Duration")
    private int duration;
    @JsonProperty("Count")
    private int count;
    @JsonProperty("User_id")
    private int user_id;

}
