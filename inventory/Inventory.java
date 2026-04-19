package sk.stuba.fiit.inventory;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.Knight;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the player party and item slots using a shared slot budget.
 *
 * <p>Slot budget: the inventory has {@code totalSlots} slots total.
 * Each non-base character costs 3 slots; the base ({@code Knight}) is free.
 * Items cost their {@link sk.stuba.fiit.items.Item#getSlotsRequired()} value.
 *
 * <p>Active character: only one character is active at a time. On death the
 * controller calls {@link #switchToNextAlive()} to swap automatically.
 * When no living character remains, {@link #isPartyDefeated()} returns {@code true}.
 *
 * <p>Item selection: a cursor ({@code selectedSlot}) cycles through the item list.
 * Items are used via {@link #useSelected(PlayerCharacter, Level)}.
 */
public class Inventory {
    private int totalSlots;
    private int usedSlots;
    private PlayerCharacter activeCharacter;
    private List<PlayerCharacter> characters;
    private List<Item> items;
    private int selectedSlot = 0;
    private PlayerCharacter baseCharacter;
    private static final Logger log = GameLogger.get(Inventory.class);

    public Inventory(int totalSlots) {
        this.totalSlots = totalSlots;
        this.usedSlots  = 0;
        this.characters = new ArrayList<>();
        this.items      = new ArrayList<>();
    }

    public Inventory() {
        this(10);
    }

    /**
     * Adds a character to the party.
     * The {@code Knight} base character is always accepted without slot cost.
     * All other characters cost 3 slots.
     *
     * @param character the character to add
     * @return {@code true} if added successfully, {@code false} if not enough slots
     */
    public boolean addCharacter(PlayerCharacter character) {
        if (character instanceof Knight) {
            baseCharacter = character;
            characters.add(character);
            if (activeCharacter == null) activeCharacter = character;
            log.info("Base character added: name={}", character.getName());
            return true;
        }
        int cost = 3;
        if (usedSlots + cost > totalSlots) {
            log.warn("Cannot add character – not enough slots: character={}, cost={}, used={}, total={}",
                character.getName(), cost, usedSlots, totalSlots);
            return false;
        }
        characters.add(character);
        if (activeCharacter == null) activeCharacter = character;
        usedSlots += cost;
        log.info("Character added: name={}, slotsUsed={}/{}",
            character.getName(), usedSlots, totalSlots);
        return true;
    }

    /**
     * Removes a non-base character from the party, freeing 3 slots.
     *
     * @param character the character to remove
     * @return {@code false} if the character is the base character or not in the party
     */
    public boolean removeCharacter(PlayerCharacter character) {
        if (character == baseCharacter) {
            log.warn("Cannot remove base character: name={}", character.getName());
            return false;
        }
        if (!characters.remove(character)) {
            log.warn("Character not found in inventory: name={}", character.getName());
            return false;
        }
        usedSlots -= 3;
        if (activeCharacter == character) {
            activeCharacter = characters.isEmpty() ? null : characters.getFirst();
            log.info("Active character changed after removal: newActive={}",
                activeCharacter != null ? activeCharacter.getName() : "none");
        }
        log.info("Character removed: name={}, slotsUsed={}/{}",
            character.getName(), usedSlots, totalSlots);
        return true;
    }

    public boolean isBaseCharacter(PlayerCharacter c) {
        return c == baseCharacter;
    }

    public boolean addItem(Item item) {
        int cost = item.getSlotsRequired();
        if (usedSlots + cost > totalSlots) {
            log.warn("Cannot add item – not enough slots: item={}, cost={}, used={}, total={}",
                item.getClass().getSimpleName(), cost, usedSlots, totalSlots);
            return false;
        }
        items.add(item);
        usedSlots += cost;
        log.info("Item added: item={}, slotsUsed={}/{}",
            item.getClass().getSimpleName(), usedSlots, totalSlots);
        return true;
    }

    public void removeItem(Item item) {
        if (items.remove(item)) {
            usedSlots -= item.getSlotsRequired();
            log.info("Item removed: item={}, slotsUsed={}/{}",
                item.getClass().getSimpleName(), usedSlots, totalSlots);
            if (selectedSlot >= items.size() && selectedSlot > 0) {
                selectedSlot = items.size() - 1;
            }
        }else {
            log.warn("Attempted to remove item not in inventory: item={}",
                item.getClass().getSimpleName());
        }
    }

    public void selectPrevious() {
        if (items.isEmpty()) return;
        selectedSlot = (selectedSlot - 1 + items.size()) % items.size();
    }

    public void selectNext() {
        if (items.isEmpty()) return;
        selectedSlot = (selectedSlot + 1) % items.size();
    }

    /**
     * Uses the currently selected item on {@code character} within the given {@code level}.
     * The level is passed as a parameter so this method does not need to call {@code GameManager}.
     *
     * @param character the active player character
     * @param level     the current level (passed from {@code PlayerController})
     */
    public void useSelected(PlayerCharacter character, Level level) {
        if (items.isEmpty() || selectedSlot >= items.size()) return;
        items.get(selectedSlot).use(character, level);
    }

    public void switchCharacter(int key) {
        if (key >= 1 && key <= characters.size()) {
            PlayerCharacter next = characters.get(key - 1);
            if (next == activeCharacter) {
                if (log.isDebugEnabled()) {
                    log.debug("switchCharacter – already active: name={}", next.getName());
                }
                return;
            }
            log.info("Character switched: from={}, to={}, key={}",
                activeCharacter.getName(), next.getName(), key);
            next.setPosition(activeCharacter.getPosition());
            next.setFacingRight(activeCharacter.isFacingRight());
            next.updateHitbox();
            activeCharacter = next;
        }else {
            log.warn("switchCharacter – invalid key: key={}, partySize={}",
                key, characters.size());
        }
    }

    /**
     * Switches to the next living character in the party, preserving position and facing.
     *
     * @return {@code true} if a living character was found and activated
     */
    public boolean switchToNextAlive() {
        Vector2D currentPosition = activeCharacter.getPosition();
        boolean  currentFacing   = activeCharacter.isFacingRight();
        for (PlayerCharacter c : characters) {
            if (c != activeCharacter && c.isAlive()) {
                log.info("Auto-switched to next alive: from={}, to={}",
                    activeCharacter.getName(), c.getName());
                c.setPosition(currentPosition);
                c.setFacingRight(currentFacing);
                c.updateHitbox();
                activeCharacter = c;
                return true;
            }
        }
        log.warn("No alive characters to switch to – party defeated");
        return false;
    }

    /**
     * Returns {@code true} when all party members have zero HP.
     *
     * @return {@code true} if the party is fully defeated
     */
    public boolean isPartyDefeated() {
        return characters.stream().noneMatch(Character::isAlive);
    }

    public int              getSelectedSlot()               { return selectedSlot; }
    public PlayerCharacter  getActive()                     { return activeCharacter; }
    public List<PlayerCharacter> getCharacters()            { return characters; }
    public List<Item>       getItems()                      { return items; }
    public int              getTotalSlots()                 { return totalSlots; }
    public int              getUsedSlots()                  { return usedSlots; }
    public int              getFreeSlots()                  { return totalSlots - usedSlots; }
}
