package sk.stuba.fiit.projectiles;

/**
 * Identifies who fired a projectile.
 *
 * <p>Used in {@code CollisionManager} instead of {@code instanceof EnemyCharacter}
 * to distinguish player-owned and enemy-owned projectiles. This decouples the
 * collision logic from the character class hierarchy.
 */
public enum ProjectileOwner {
    /** The projectile was fired by the active player character. */
    PLAYER,
    /** The projectile was fired by an enemy character. */
    ENEMY
}
