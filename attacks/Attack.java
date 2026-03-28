package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

public interface Attack {
    void execute(Character caster, Level level);
    String getAnimationName();
    float getAnimationDuration(AnimationManager animManager);
}
