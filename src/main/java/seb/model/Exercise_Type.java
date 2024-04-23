package seb.model;

public enum Exercise_Type {
    PushUps("pushups"),
    SitUps("situps");

    private String type;

    Exercise_Type(String type) { this.type = type; }

    public String getType() { return type; }

}
