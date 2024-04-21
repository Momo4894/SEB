package seb.dal;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String message) { super(message); }

    public ServiceNotFoundException(String message, Throwable cause) { super(message); }

    public ServiceNotFoundException(Throwable cause) { super(cause); }
}
