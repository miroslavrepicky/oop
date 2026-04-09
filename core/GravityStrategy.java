package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.Character;

public interface GravityStrategy {
    void apply(Character character, float deltaTime);
}
