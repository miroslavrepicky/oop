package sk.stuba.fiit.core.exceptions;

/**
 * Thrown when saving the game to disk fails.
 *
 * <p>Distinguishes save-related I/O failures from other game errors so that
 * callers can handle them separately (e.g. display a "Save failed" notice
 * without crashing the game). Wraps the underlying {@link java.io.IOException}
 * as the cause so the full stack trace is preserved in the log.
 */
public class SaveException extends ShadowQuestException {
    /**
     * @param message human-readable description of the failure
     * @param cause   the underlying I/O exception that triggered this error
     */
    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
