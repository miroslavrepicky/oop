package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Character;

public class NoGravity implements GravityStrategy {
    @Override
    public void apply(Character character, float deltaTime) {
        // ziadna gravitacia – projektily
    }
}
