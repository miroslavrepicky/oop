package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.EnemyCharacter;

public interface StatusEffect {
    void tick(float deltaTime);
    boolean isExpired();
    EnemyCharacter getTarget();
}
