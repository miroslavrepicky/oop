package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.attacks.StatusEffect;
import sk.stuba.fiit.characters.Character;

@FunctionalInterface
public interface StatusEffectFactory {
    StatusEffect create(Character target);
}
