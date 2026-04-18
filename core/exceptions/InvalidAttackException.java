package sk.stuba.fiit.core.exceptions;

public class InvalidAttackException extends ShadowQuestException {
    private final String attackerName;
    private final String reason;

    public InvalidAttackException(String attackerName, String reason) {
        super("Invalid attack by " + attackerName + ": " + reason);
        this.attackerName = attackerName;
        this.reason = reason;
    }

    public String getAttackerName() { return attackerName; }
    public String getReason()       { return reason; }
}
