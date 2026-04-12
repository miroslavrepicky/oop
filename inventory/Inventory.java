package sk.stuba.fiit.inventory;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.Knight;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private int totalSlots;
    private int usedSlots;
    private PlayerCharacter activeCharacter;
    private List<PlayerCharacter> characters;
    private List<Item> items;
    private int selectedSlot = 0; // aktualne vybrany slot
    private PlayerCharacter baseCharacter;

    public Inventory(int totalSlots) {
        this.totalSlots = totalSlots;
        this.usedSlots  = 0;
        this.characters = new ArrayList<>();
        this.items      = new ArrayList<>();
    }

    public Inventory() {
        this(10);
    }

    public boolean addCharacter(PlayerCharacter character) {
        if (character instanceof Knight) {
            baseCharacter = character;
            characters.add(character);
            if (activeCharacter == null) activeCharacter = character;
            return true; // bez slot cost
        }
        int cost = 3;
        if (usedSlots + cost > totalSlots) return false;
        characters.add(character);
        if (activeCharacter == null) activeCharacter = character;
        usedSlots += cost;
        return true;
    }

    public boolean removeCharacter(PlayerCharacter character) {
        if (character == baseCharacter) return false; // Knight sa neda odstranit
        if (!characters.remove(character)) return false;
        usedSlots -= 3;
        if (activeCharacter == character) {
            activeCharacter = characters.isEmpty() ? null : characters.get(0);
        }
        return true;
    }

    public boolean isBaseCharacter(PlayerCharacter c) {
        return c == baseCharacter;
    }

    public boolean addItem(Item item) {
        int cost = item.getSlotsRequired();
        if (usedSlots + cost > totalSlots) return false;
        items.add(item);
        usedSlots += cost;
        return true;
    }

    public void removeItem(Item item) {
        if (items.remove(item)) {
            usedSlots -= item.getSlotsRequired();
            // oprav selectedSlot ak vysiel mimo
            if (selectedSlot >= items.size() && selectedSlot > 0) {
                selectedSlot = items.size() - 1;
            }
        }
    }

    /** Posunie vyber o jeden slot dolava (Q). */
    public void selectPrevious() {
        if (items.isEmpty()) return;
        selectedSlot = (selectedSlot - 1 + items.size()) % items.size();
    }

    /** Posunie vyber o jeden slot doprava (W). */
    public void selectNext() {
        if (items.isEmpty()) return;
        selectedSlot = (selectedSlot + 1) % items.size();
    }

    /** Pouzije aktualne vybrany item (E). */
    public void useSelected(PlayerCharacter character) {
        if (items.isEmpty() || selectedSlot >= items.size()) return;
        items.get(selectedSlot).use(character);
    }

    public void switchCharacter(int key) {
        if (key >= 1 && key <= characters.size()) {
            PlayerCharacter next = characters.get(key - 1);
            if (next == activeCharacter) return;

            // Prenesie poziciu a smer pohladu na novu postavu
            next.setPosition(activeCharacter.getPosition());
            next.setFacingRight(activeCharacter.isFacingRight());
            next.updateHitbox();

            activeCharacter = next;
        }
    }

    public boolean switchToNextAlive() {
        Vector2D currentPosition  = activeCharacter.getPosition();
        boolean  currentFacing    = activeCharacter.isFacingRight();

        for (PlayerCharacter c : characters) {
            if (c != activeCharacter && c.isAlive()) {
                c.setPosition(currentPosition);
                c.setFacingRight(currentFacing);
                c.updateHitbox();
                activeCharacter = c;
                return true;
            }
        }
        return false;
    }

    public boolean isPartyDefeated() {
        return characters.stream().noneMatch(Character::isAlive);
    }

    public int getSelectedSlot()              { return selectedSlot; }
    public PlayerCharacter getActive()        { return activeCharacter; }
    public List<PlayerCharacter> getCharacters() { return characters; }
    public List<Item> getItems()              { return items; }
    public int getTotalSlots()                { return totalSlots; }
    public int getUsedSlots()                 { return usedSlots; }
    public int getFreeSlots()                 { return totalSlots - usedSlots; }
}
