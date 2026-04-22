package sk.stuba.fiit.core.exceptions;

/**
 * Výnimka pri zlyhaní uloženia hry.
 * Rozlišuje chyby ukladania od ostatných herných chýb.
 */
public final class SaveException extends ShadowQuestException {
    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
