package sk.stuba.fiit.core.exceptions;

public class ShadowQuestException extends RuntimeException {
    public ShadowQuestException(String message) {
        super(message);
    }
    public ShadowQuestException(String message, Throwable cause) {
        super(message, cause);
    }
}
