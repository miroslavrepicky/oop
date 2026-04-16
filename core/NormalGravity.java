package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;

import java.util.Collections;
import java.util.List;

public class NormalGravity implements GravityStrategy {
    private static final float GRAVITY = -500f;

    @Override
    public void apply(Character character, float deltaTime, List<Rectangle> platforms) {
        character.setVelocityY(character.getVelocityY() + GRAVITY * deltaTime);
        float newY      = character.getPosition().getY() + character.getVelocityY() * deltaTime;
        float currentX  = character.getPosition().getX();
        boolean onGround = false;

        List<Rectangle> walls = (platforms != null) ? platforms : Collections.emptyList();

        Rectangle charBox = new Rectangle(
            currentX, newY,
            character.getHitbox().width, character.getHitbox().height
        );

        for (Rectangle platform : walls) {
            if (!charBox.overlaps(platform)) continue;

            float overlapY = Math.min(charBox.y + charBox.height, platform.y + platform.height)
                - Math.max(charBox.y, platform.y);
            float overlapX = Math.min(charBox.x + charBox.width, platform.x + platform.width)
                - Math.max(charBox.x, platform.x);

            if (overlapY <= overlapX) {
                if (character.getVelocityY() < 0) {
                    newY = platform.y + platform.height;
                    character.setVelocityY(0f);
                    onGround = true;
                } else if (character.getVelocityY() > 0) {
                    newY = platform.y - charBox.height;
                    character.setVelocityY(0f);
                }
                charBox.y = newY;
            }
        }

        character.getPosition().setY(newY);
        character.setOnGround(onGround);
        character.updateHitbox();
    }
}
