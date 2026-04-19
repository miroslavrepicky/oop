package sk.stuba.fiit.core.exceptions;


/**
 * Thrown when an attack is executed with invalid parameters,
 * e.g., a {@code null} wrapped attack in a decorator.
 */
public class InvalidAttackException extends ShadowQuestException {
    private final String attackerName;
    private final String reason;

    public InvalidAttackException(String attackerName, String reason) {
        super("Invalid attack by " + attackerName + ": " + reason);
        this.attackerName = attackerName;
        this.reason = reason;
    }
    /** @return the name of the character that attempted the invalid attack */
    public String getAttackerName() { return attackerName; }
    /** @return a description of why the attack is invalid */
    public String getReason()       { return reason; }
}
