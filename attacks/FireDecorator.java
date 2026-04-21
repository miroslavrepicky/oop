package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

/**
 * Decorator that adds a Damage-over-Time burn effect on top of a base projectile attack.
 *
 * <p>Works with any attack that spawns a projectile ({@link SpellAttack}, {@link ArrowAttack}).
 * After the wrapped attack creates and adds the projectile to the level,
 * this decorator attaches the DOT effect directly to the returned projectile.
 * {@code CollisionManager} then calls {@link Character#applyDot(int, float)}
 * on whatever character the projectile hits.
 *
 * <p>Melee attacks return {@code null} from {@code execute()} – the decorator
 * handles this gracefully by doing nothing.
 */
public class FireDecorator extends AttackDecorator {

    private static final int   BURN_DPS      = 8;
    private static final float BURN_DURATION = 3.0f;
    private static final int   EXTRA_MANA    = 15;

    public FireDecorator(Attack wrapped) { super(wrapped); }

    @Override
    public Projectile execute(Character attacker, Level level) {
        Projectile p = wrapped.execute(attacker, level);
        if (p != null) {
            p.setDotEffect(BURN_DPS, BURN_DURATION);
            p.setTint(1f, 0.3f, 0f);
        }
        return p;
    }

    @Override
    public int getManaCost() { return wrapped.getManaCost() + EXTRA_MANA; }
}
