package sk.stuba.fiit.core.engine;

/**
 * Contract for objects that respond to collision events.
 *
 * <p>Called by {@code CollisionManager} when two objects overlap.
 * The {@code other} parameter is typed as {@code Object} to avoid coupling
 * the engine layer to specific game entity types.
 */
public interface Collidable {
    /**
     * Called when this object collides with {@code other}.
     *
     * @param other the object this instance collided with
     */
    void onCollision(Object other);
}
