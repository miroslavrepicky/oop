package sk.stuba.fiit.core.engine;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.util.Vector2D;

/**
 * Contract that must be implemented by every character controlled by AI.
 *
 * <p>AIController depends exclusively on this interface and has no
 * knowledge of {@code EnemyCharacter} or any other concrete class. This makes
 * it possible to add new enemy types or NPCs without modifying the controller.
 */
public interface AIControllable {

    // position and movement
    Vector2D getPosition();
    void     move(Vector2D direction);
    void     jump(float force);
    boolean  isOnGround();
    boolean  wasLastMoveBlocked();
    float    getSpeed();

    // orientation
    void    setFacingRight(boolean right);
    boolean isFacingRight();

    // velocity (synchronisation with physics)
    void setVelocityX(float vx);

    /**
     * Returns {@code true} if the player character is within detection range.
     *
     * @param player the active player character
     * @return {@code true} if the player is detected
     */
    boolean detectPlayer(PlayerCharacter player);

    /**
     * Triggers the enemy's attack sequence (cooldown check, animation start).
     * Actual damage/projectile spawn happens later in {@code EnemyCharacter.update()}
     * via {@code attack.execute()} – the AI trigger and the execution are intentionally
     * separated so the attack animation plays before impact.
     *
     * <p>No player reference is needed here: the target is resolved at execution time
     * from the level context inside {@code Attack.execute(attacker, level)}.
     */
    void triggerAttack();

    /**
     * Returns {@code true} if the enemy is currently playing an attack animation.
     *
     * @return {@code true} while the attack animation timer is active
     */
    boolean isAttacking();
}
