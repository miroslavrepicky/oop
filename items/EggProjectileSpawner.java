package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Marker item created when a duck is killed and the "egg" outcome (50 %) is rolled.
 *
 * <p>This item cannot be picked up into the inventory ({@link #onPickup} is a no-op).
 * Instead, {@code Level.update()} detects it via {@code instanceof} check, spawns
 * an {@link sk.stuba.fiit.projectiles.EggProjectile} at its position, and immediately
 * removes it from the scene.
 *
 * <p>Why a marker item instead of a direct spawn?
 * {@code Duck.onKilled()} returns an {@code Item} – using a marker avoids
 * changing that method's signature and the {@code CollisionManager} logic.
 * The level decides what to do with the marker on its own.
 */
public class EggProjectileSpawner extends Item {

    public EggProjectileSpawner(Vector2D position) {
        super(0, position); // 0 slotov – neberie sa do inventara
    }

    @Override
    public void use(PlayerCharacter character, Level level, Inventory inventory) {

    }

    /**
     * Blocks pick-up. It just spawns an explosion.
     */
    @Override
    public String getIconPath() { return null; }

    @Override
    public boolean onPickup(PlayerCharacter character, Inventory inventory) {
        return false;   // vajce sa nedá zdvihnut
    }
}
