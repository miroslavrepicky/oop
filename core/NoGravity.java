package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class NoGravity implements GravityStrategy {
    @Override
    public void apply(Physicable body, float deltaTime, List<Rectangle> platforms) {
        // žiadna gravitácia – projektily bez fyziky, debug objekty
    }
}
