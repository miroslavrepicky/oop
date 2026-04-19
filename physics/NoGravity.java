package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.engine.Physicable;

import java.util.List;


/**
 * No-op gravity strategy used for horizontally flying projectiles and debug objects.
 *
 * <p>Applying this strategy leaves the body's position and velocity unchanged.
 * It is the default strategy for {@link sk.stuba.fiit.projectiles.Projectile}.
 */
public class NoGravity implements GravityStrategy {
    @Override
    public void apply(Physicable body, float deltaTime, List<Rectangle> platforms) {
        // žiadna gravitácia – projektily bez fyziky, debug objekty
    }
}
