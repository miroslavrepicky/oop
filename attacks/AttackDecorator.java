package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.world.Level;

/**
 * Abstraktný dekorátor pre {@link Attack}.
 *
 * Vzor Decorator: obaľuje existujúci Attack a pridáva správanie
 * bez zmeny pôvodnej triedy. Každý konkrétny dekorátor override-ne
 * len to čo mení – zvyšok deleguje na wrapped.
 *
 * Príklad použitia:
 * <pre>
 *   Attack base   = new SpellAttack(6.0f, 100f, 20);
 *   Attack fire   = new FireSpellDecorator(base);
 *   Attack frozen = new FreezeSpellDecorator(base);
 *
 *   // Stacking (fire + freeze = chaos):
 *   Attack both   = new FreezeSpellDecorator(new FireSpellDecorator(base));
 * </pre>
 *
 * Prečo abstract a nie interface default?
 * Dekorátor musí uložiť referenciu na wrapped – to vyžaduje pole
 * a konštruktor, čo interface nevie elegantne vyjadriť.
 */
public abstract class AttackDecorator implements Attack {

    /** Zabalená implementácia – voláme ju pre všetko čo nemeníme. */
    protected final Attack wrapped;

    protected AttackDecorator(Attack wrapped) {
        if (wrapped == null) {
            throw new InvalidAttackException("unknown", "wrapped Attack nesmie byt null");
        }
        this.wrapped = wrapped;
    }

    // -------------------------------------------------------------------------
    //  Predvolené delegovanie – podtriedy override-nú len to čo menia
    // -------------------------------------------------------------------------

    @Override
    public void execute(Character attacker, Level level) {
        wrapped.execute(attacker, level);
    }

    @Override
    public String getAnimationName() {
        return wrapped.getAnimationName();
    }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        return wrapped.getAnimationDuration(am);
    }

    @Override
    public int getManaCost() {
        return wrapped.getManaCost();
    }
}
