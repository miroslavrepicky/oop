package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.engine.Physicable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class FlyingGravity implements GravityStrategy {
    private static final float MAX_DIFFERENCE = 150f;


    @Override
    public void apply(Physicable body, float deltaTime, List<Rectangle> platforms) {
        int randomSign = ThreadLocalRandom.current().nextBoolean() ? 15 : -20;
        body.setVelocityY(Math.clamp(body.getVelocityY() + randomSign * deltaTime, -MAX_DIFFERENCE, MAX_DIFFERENCE));
        float newY     = body.getPosition().getY() + body.getVelocityY() * deltaTime;
        float currentX = body.getPosition().getX();
        boolean onGround = false;

        List<Rectangle> walls = (platforms != null) ? platforms : Collections.emptyList();

        Rectangle charBox = new Rectangle(
            currentX, newY,
            body.getHitbox().width, body.getHitbox().height
        );

        for (Rectangle platform : walls) {
            if (!charBox.overlaps(platform)) continue;

            float overlapY = Math.min(charBox.y + charBox.height, platform.y + platform.height)
                - Math.max(charBox.y, platform.y);
            float overlapX = Math.min(charBox.x + charBox.width, platform.x + platform.width)
                - Math.max(charBox.x, platform.x);

            if (overlapY <= overlapX) {
                if (body.getVelocityY() < 0) {
                    newY = platform.y + platform.height;
                    body.setVelocityY(0f);
                    onGround = true;
                } else if (body.getVelocityY() > 0) {
                    newY = platform.y - charBox.height;
                    body.setVelocityY(0f);
                }
                charBox.y = newY;
            }
        }

        body.getPosition().setY(newY);
        body.setOnGround(onGround);
        body.updateHitbox();
    }
}
