package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

/**
 * Decorator that adds a slow/freeze effect on top of a base projectile attack.
 *
 * <p>Works with any attack that spawns a projectile ({@link SpellAttack}, {@link ArrowAttack}).
 * After the wrapped attack creates and adds the projectile to the level,
 * this decorator attaches the slow effect directly to the returned projectile.
 * {@code CollisionManager} then calls {@link Character#applySlow(float, float)}
 * on whatever character the projectile hits.
 *
 * <p>Melee attacks return {@code null} from {@code execute()} – the decorator
 * handles this gracefully by doing nothing.
 */
public class FreezeDecorator extends AttackDecorator {

    private static final float SLOW_MULTIPLIER = 0.3f;
    private static final float SLOW_DURATION   = 2.5f;
    private static final int   EXTRA_MANA      = 10;

    public FreezeDecorator(Attack wrapped) { super(wrapped); }

    @Override
    public Projectile execute(Character attacker, Level level) {
        Projectile p = wrapped.execute(attacker, level);
        if (p != null) {
            p.setSlowEffect(SLOW_MULTIPLIER, SLOW_DURATION);
            p.setTint(0.3f, 0.7f, 1f);
        }
        return p;
    }

    @Override
    public int getManaCost() { return wrapped.getManaCost() + EXTRA_MANA; }
}
