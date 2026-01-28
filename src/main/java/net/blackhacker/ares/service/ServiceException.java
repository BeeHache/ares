package net.blackhacker.ares.service;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message, Object... cause) {
        super(String.format(message, cause));
    }
}
