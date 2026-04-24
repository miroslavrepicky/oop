package sk.stuba.fiit.core.exceptions;

/**
 * Thrown when the game reaches an illegal or inconsistent state,
 * e.g. starting a level without an active player, or requesting an invalid level number.
 */
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
    /** @return a human-readable description of the location where the error occurred */
    public String getContext() { return context; }
}
