package sk.stuba.fiit.core.exceptions;

/**
 * Vynimka pri zlyhani ulozenia hry.
 * Rozlisuje chyby ukladania od ostatnych hernych chyb.
 */
public class SaveException extends ShadowQuestException {
    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
