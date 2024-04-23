package seb.model;

public enum Status {
    PENDING("pending"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String status;

    Status (String status) {
        this.status = status;
    }
    public String getStatus() { return status; }
}
