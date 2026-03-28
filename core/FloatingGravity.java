package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.world.Level;

public class FloatingGravity implements GravityStrategy {
    private static final float GRAVITY = -50f; // slaba gravitacia

    @Override
    public void apply(Character character, float deltaTime) {
        character.setVelocityY(character.getVelocityY() + GRAVITY * deltaTime);
        float newY = character.getPosition().getY() + character.getVelocityY() * deltaTime;

        Level level = GameManager.getInstance().getCurrentLevel();
        boolean onGround = false;

        if (level != null && level.getMapManager() != null) {
            Rectangle charBox = new Rectangle(
                character.getPosition().getX(),
                newY,
                32, 32
            );

            for (Rectangle platform : level.getMapManager().getHitboxes()) {
                if (charBox.overlaps(platform)) {
                    if (character.getVelocityY() < 0) {
                        newY = platform.y + platform.height;
                        character.setVelocityY(0f);
                        onGround = true;
                    }
                }
            }
        }

        character.getPosition().setY(newY);
        character.setOnGround(onGround);
        character.updateHitbox();
    }
}
