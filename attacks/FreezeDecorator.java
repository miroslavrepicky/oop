package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
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
    private static final int   DISCOUNT_MANA   = 5;

    public FreezeDecorator(Attack wrapped) {
        super(wrapped);
    }

    @Override
    public Projectile execute(Character attacker, Level level) {
        Projectile projectile = wrapped.execute(attacker, level);
        if (projectile != null) {
            projectile.setSlowEffect(SLOW_MULTIPLIER, SLOW_DURATION);
        }
        return projectile;
    }

    @Override
    public String getAnimationName() {
        return "cast_freeze";
    }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        if (am != null && am.hasAnimation("cast_freeze")) {
            return am.getAnimationDuration("cast_freeze");
        }
        return wrapped.getAnimationDuration(am);
    }

    @Override
    public int getManaCost() {
        return Math.max(0, wrapped.getManaCost() - DISCOUNT_MANA);
    }
}
