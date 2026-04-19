package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.exceptions.InvalidAttackException;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

/**
 * Abstract base class for attack decorators following the Decorator pattern.
 *
 * <p>Wraps an existing {@link Attack} and adds behaviour without modifying
 * the original class. Each concrete decorator overrides only what it changes;
 * everything else is delegated to the wrapped attack.
 *
 * <p>Because {@link Attack#execute(Character, Level)} returns the spawned
 * {@link Projectile}, decorators can attach on-hit effects to it directly
 * without any knowledge of the wrapped attack's internals.
 *
 * <p>Example usage:
 * <pre>
 *   Attack base   = new SpellAttack(6.0f, 100f, 20);
 *   Attack fire   = new FireDecorator(base);
 *   Attack freeze = new FreezeDecorator(base);
 *
 *   // Stacking decorators:
 *   Attack both = new FreezeDecorator(new FireDecorator(base));
 * </pre>
 */
public abstract class AttackDecorator implements Attack {

    protected final Attack wrapped;

    protected AttackDecorator(Attack wrapped) {
        if (wrapped == null) {
            throw new InvalidAttackException("unknown", "wrapped Attack nesmie byt null");
        }
        this.wrapped = wrapped;
    }

    @Override
    public Projectile execute(Character attacker, Level level) {
        return wrapped.execute(attacker, level);
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
