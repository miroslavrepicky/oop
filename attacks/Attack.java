package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

public interface Attack {
    /**
     * Vykoná útok. Útočníkom môže byť PlayerCharacter aj EnemyCharacter –
     * obe sú podtriedy Character. Implementácia si sama určí cieľ podľa
     * typu útočníka (hráč → nepriatelia, nepriateľ → hráč).
     */
    void execute(Character attacker, Level level);

    /** Názov animácie ktorú má útočník prehrať (napr. "attack", "cast"). */
    String getAnimationName();

    /** Dĺžka animácie v sekundách – určuje kedy skončí attackAnimTimer. */
    float getAnimationDuration(AnimationManager am);

    /** Mana cost – 0 pre ne-spell útoky. */
    default int getManaCost() { return 0; }
}
