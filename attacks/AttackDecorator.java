package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.world.Level;

/**
 * Abstract base class for attack decorators following the Decorator pattern.
 *
 * <p>Wraps an existing {@link Attack} and adds behaviour without modifying
 * the original class. Each concrete decorator overrides only what it changes;
 * everything else is delegated to the wrapped attack.
 *
 * <p>Example usage:
 * <pre>
 *   Attack base = new SpellAttack(6.0f, 100f, 20);
 *   Attack fire = new FireSpellDecorator(base);
 *   Attack frozen = new FreezeSpellDecorator(base);
 *
 *   // Stacking decorators:
 *   Attack both = new FreezeSpellDecorator(new FireSpellDecorator(base));
 * </pre>
 *
 * <p>An abstract class is used instead of an interface default method because
 * the decorator must store a reference to the wrapped instance, which requires
 * a field and a constructor.
 */
public abstract class AttackDecorator implements Attack {

    /** The wrapped attack implementation – delegated to for all unchanged behaviour. */
    protected final Attack wrapped;

    /**
     * Constructs a decorator around the given attack.
     *
     * @param wrapped the attack to wrap; must not be {@code null}
     * @throws InvalidAttackException if {@code wrapped} is {@code null}
     */
    protected AttackDecorator(Attack wrapped) {
        if (wrapped == null) {
            throw new InvalidAttackException("unknown", "wrapped Attack nesmie byt null");
        }
        this.wrapped = wrapped;
    }

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
