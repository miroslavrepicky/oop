package sk.stuba.fiit.core.exceptions;


/**
 * Root unchecked exception for all ShadowQuest-specific errors.
 * Extend this class for domain-specific exception types.
 */
public class ShadowQuestException extends RuntimeException {
    public ShadowQuestException(String message) {
        super(message);
    }
    public ShadowQuestException(String message, Throwable cause) {
        super(message, cause);
    }
}
