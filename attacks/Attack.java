package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

/**
 * Contract for all attack strategies used by player and enemy characters.
 *
 * <p>Follows the Strategy pattern: each concrete implementation encapsulates
 * a distinct attack behavior (melee swing, arrow, magic spell, etc.).
 *
 * <p>{@link #execute(Character, Level)} returns the spawned {@link Projectile}
 * so that decorators ({@link FireDecorator}, {@link FreezeDecorator}) can attach
 * on-hit effects to it without knowing the concrete projectile type or modifying
 * the wrapped attack's internal state. Melee attacks return {@code null}.
 */
public interface Attack {

    /**
     * Executes the attack and returns the spawned projectile, or {@code null}
     * for melee attacks that do not spawn a projectile.
     *
     * @param attacker the character performing the attack
     * @param level    the current game level
     * @return the spawned {@link Projectile}, or {@code null}
     */
    Projectile execute(Character attacker, Level level);

    /**
     * Returns the animation name that the attacker should play when this attack fires.
     *
     * @return animation key understood by {@link AnimationManager}
     */
    String getAnimationName();

    /**
     * Returns the total animation duration in seconds.
     *
     * @param am the attacker's {@link AnimationManager}; may be {@code null}
     * @return animation duration in seconds, or a sensible default if {@code am} is null
     */
    float getAnimationDuration(AnimationManager am);

    /**
     * Mana cost of this attack. Returns {@code 0} for non-spell attacks.
     *
     * @return mana points consumed per use
     */
    default int getManaCost() { return 0; }

    /**
     * Returns the duration of a single animation frame in seconds.
     *
     * @param am the attacker's {@link AnimationManager}; may be {@code null}
     * @return per-frame duration, or {@code 0.08f} as fallback
     */
    default float getFrameDuration(AnimationManager am) {
        return am != null && am.hasAnimation(getAnimationName())
            ? am.getAnimationDuration(getAnimationName())
              / am.getFrameCount(getAnimationName())
            : 0.08f;
    }
}
