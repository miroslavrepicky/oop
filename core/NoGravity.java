package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;

import java.util.List;

public class NoGravity implements GravityStrategy {
    @Override
    public void apply(Character character, float deltaTime, List<Rectangle> platforms) {
        // žiadna gravitácia – projektily, debug objekty
    }
}
