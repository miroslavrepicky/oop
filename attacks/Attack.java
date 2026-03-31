package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

public interface Attack {
    /**
     * Vykona utok. utocnikom moze byt PlayerCharacter aj EnemyCharacter –
     * obe su podtriedy Character. Implementacia si sama urci ciel podla
     * typu utocnika (hrac -> nepriatelia, nepriatel -> hrac).
     */
    void execute(Character attacker, Level level);

    /** Nazov animacie ktoru ma utocnik prehrat (napr. "attack", "cast"). */
    String getAnimationName();

    /** Dĺzka animacie v sekundach – urcuje kedy skonci attackAnimTimer. */
    float getAnimationDuration(AnimationManager am);

    /** Mana cost – 0 pre ne-spell utoky. */
    default int getManaCost() { return 0; }
}
