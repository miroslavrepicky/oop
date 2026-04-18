package sk.stuba.fiit.core.exceptions;


public class GameStateException extends ShadowQuestException {
    private final String context;

    public GameStateException(String message, String context) {
        super(message);
        this.context = context;
    }

    public GameStateException(String message, String context, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public String getContext() { return context; }
}
