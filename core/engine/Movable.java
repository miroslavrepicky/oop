package sk.stuba.fiit.core.engine;

import sk.stuba.fiit.util.Vector2D;

/**
 * Contract for objects that support directional movement.
 *
 * <p>Separates the movement capability from the full {@code Character} contract
 * so that lightweight objects or decorators can implement movement without
 * carrying all character state.
 */
public interface Movable {
    /**
     * Moves the object by the given direction vector.
     *
     * @param direction displacement vector for this frame
     */
    void move(Vector2D direction);
}
