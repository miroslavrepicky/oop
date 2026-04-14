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
        float currentX = character.getPosition().getX();

        Level level = GameManager.getInstance().getCurrentLevel();
        boolean onGround = false;

        if (level != null && level.getMapManager() != null) {
            Rectangle charBox = new Rectangle(currentX, newY,
                character.getHitbox().width, character.getHitbox().height);

            for (Rectangle platform : level.getMapManager().getHitboxes()) {
                if (!charBox.overlaps(platform)) continue;

                // Vypocítaj hlbku prieniku na oboch osiach
                float overlapY = Math.min(charBox.y + charBox.height, platform.y + platform.height)
                    - Math.max(charBox.y, platform.y);
                float overlapX = Math.min(charBox.x + charBox.width, platform.x + platform.width)
                    - Math.max(charBox.x, platform.x);

                // Reaguj len na vertikalnu koliziu (Y-prienik je mensí ako X-prienik)
                if (overlapY <= overlapX) {
                    if (character.getVelocityY() < 0) {
                        // pad nadol – pristatie na vrchu platformy
                        newY = platform.y + platform.height;
                        character.setVelocityY(0f);
                        onGround = true;
                    } else if (character.getVelocityY() > 0) {
                        // skok nahor - naraz do strechy
                        newY = platform.y - charBox.height;
                        character.setVelocityY(0f);
                    }
                    charBox.y = newY; // aktualizuj pre dalsie platformy
                }
                // Horizontalna kolizia (vacsi Y-prienik ako X) - gravitacia neriesi,
                // to je uloha horizontalneho pohybu v PlayerController
            }
        }

        character.getPosition().setY(newY);
        character.setOnGround(onGround);
        character.updateHitbox();
    }
}
