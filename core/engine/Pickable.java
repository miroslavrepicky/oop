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
     * Typically, adds the item to the player's inventory.
     *
     * @param character the player who picked up this object
     */
    void onPickup(PlayerCharacter character);
}
