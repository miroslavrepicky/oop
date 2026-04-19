package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;


/**
 * Contract for all attack strategies used by player and enemy characters.
 *
 * <p>Follows the Strategy pattern: each concrete implementation encapsulates
 * a distinct attack behaviour (melee swing, arrow, magic spell, etc.).
 * The attacker can be either a {@code PlayerCharacter} or an {@code EnemyCharacter}
 * – both extend {@code Character}. The implementation decides the target
 * based on the attacker type (player → enemies, enemy → player).
 *
 * <p>The {@link #getAnimationName()} and {@link #getAnimationDuration(AnimationManager)}
 * methods allow the caller to drive the animation without knowing the concrete type.
 */
public interface Attack {
    /**
     * Executes the attack. The attacker may be a player or an enemy character.
     * The implementation resolves the target from the {@code level} context.
     *
     * @param attacker the character performing the attack
     * @param level    the current game level providing access to enemies and projectile lists
     */
    Projectile execute(Character attacker, Level level);

    /**
     * Returns the animation name that the attacker should play when this attack fires
     * (e.g. {@code "attack"}, {@code "cast"}).
     *
     * @return animation key understood by {@link AnimationManager}
     */
    String getAnimationName();

    /**
     * Returns the total animation duration in seconds. Used to determine when the
     * {@code attackAnimTimer} expires and the attack state ends.
     *
     * @param am the attacker's {@link AnimationManager}; may be {@code null}
     * @return animation duration in seconds, or a sensible default if {@code am} is null
     */
    float getAnimationDuration(AnimationManager am);

    /**
     * Mana cost of this attack. Returns {@code 0} for non-spell attacks.
     * {@code Wizzard} checks this before allowing execution.
     *
     * @return mana points consumed per use
     */
    default int getManaCost() { return 0; }

    /**
     * Returns the duration of a single animation frame in seconds.
     * Used to time the projectile spawn relative to the attack animation.
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
