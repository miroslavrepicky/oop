package sk.stuba.fiit.core.exceptions;

public class InventoryException extends ShadowQuestException {
    private final int usedSlots;
    private final int totalSlots;

    public InventoryException(String message, int usedSlots, int totalSlots) {
        super(message);
        this.usedSlots  = usedSlots;
        this.totalSlots = totalSlots;
    }

    public int getUsedSlots()  { return usedSlots; }
    public int getTotalSlots() { return totalSlots; }
    public int getFreeSlots()  { return totalSlots - usedSlots; }
}
