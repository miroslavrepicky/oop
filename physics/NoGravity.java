package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.engine.Physicable;

import java.util.List;

public class NoGravity implements GravityStrategy {
    @Override
    public void apply(Physicable body, float deltaTime, List<Rectangle> platforms) {
        // žiadna gravitácia – projektily bez fyziky, debug objekty
    }
}
