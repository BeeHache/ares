package net.blackhacker.ares.service;

public class RegistrationException extends ServiceException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationException(String message, Object ...cause) {
        super(message, cause);
    }
}
