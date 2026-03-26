package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Character;

public class FloatingGravity implements GravityStrategy {
    private static final float GRAVITY = -50f; // slabá gravitácia
    private static final float GROUND_Y = 100f;

    @Override
    public void apply(Character character, float deltaTime) {
        character.setVelocityY(character.getVelocityY() + GRAVITY * deltaTime);
        character.getPosition().setY(character.getPosition().getY() + character.getVelocityY() * deltaTime);

        if (character.getPosition().getY() <= GROUND_Y) {
            character.getPosition().setY(GROUND_Y);
            character.setVelocityY(0f);
            character.setOnGround(true);
        } else {
            character.setOnGround(false);
        }

        character.updateHitbox();
    }
}
