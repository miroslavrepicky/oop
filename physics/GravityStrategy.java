package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.engine.Physicable;

import java.util.List;

/**
 * Strategy interface for applying gravity to any {@link Physicable} object.
 *
 * <p>Decouples gravity logic from the character hierarchy: the same strategy
 * implementations ({@link NormalGravity}, {@link FloatingGravity}, {@link NoGravity})
 * work transparently for {@code Character} subclasses as well as {@code Projectile}
 * instances, without any changes to the strategy code.
 */
public interface GravityStrategy {
    /**
     * Applies gravity to the given physical body for one frame.
     *
     * @param body      the object to apply gravity to
     * @param deltaTime time elapsed since the last frame in seconds
     * @param platforms collision rectangles from the map; {@code null} or empty means no platforms
     */
    void apply(Physicable body, float deltaTime, List<Rectangle> platforms);
}
