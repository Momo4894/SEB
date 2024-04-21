package seb.dal;

public class DataAccessException extends RuntimeException{
    public DataAccessException(String message) { super(message); }

    public DataAccessException(String message, Throwable cause) { super(message); }

    public DataAccessException(Throwable cause) { super(cause); }
}
