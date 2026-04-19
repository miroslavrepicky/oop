package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;

/**
 * Contract for time-based status effects applied to enemy characters.
 *
 * <p>Implementations (e.g. {@code BurnEffect}, {@code FreezeEffect}) are stored
 * in the active {@code Level} and ticked every frame via {@link #tick(float)}.
 * Once {@link #isExpired()} returns {@code true} the level removes the effect
 * from its list automatically.
 */
public interface StatusEffect {
    /**
     * Advances the effect by {@code deltaTime} seconds.
     * Called every frame from {@code Level.update()}.
     *
     * @param deltaTime time elapsed since the last frame in seconds
     */
    void tick(float deltaTime);

    /**
     * Returns {@code true} when the effect has run its full duration
     * or the target is no longer alive.
     *
     * @return {@code true} if the effect should be removed from the level
     */
    boolean isExpired();

    /**
     * Returns the enemy character this effect is applied to.
     *
     * @return the affected {@link Character}
     */
    Character getTarget();
}
