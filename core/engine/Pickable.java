package sk.stuba.fiit.core.engine;

import sk.stuba.fiit.characters.PlayerCharacter;

/**
 * Contract for world objects that can be picked up by the player.
 *
 * <p>Called by {@code CollisionManager.pickupNearbyItem()} when the player
 * presses the pick-up key while overlapping the item's hitbox.
 */
public interface Pickable {
    /**
     * Handles the pick-up action for the given player character.
     *
     * @param character the player who picked up this object
     * @return true if the item was successfully picked up, false if inventory is full
     */
    boolean onPickup(PlayerCharacter character);
}
