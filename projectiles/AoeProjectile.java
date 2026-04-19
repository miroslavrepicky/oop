package sk.stuba.fiit.projectiles;

/**
 * Marker interface for projectiles that deal area-of-effect damage on impact.
 *
 * <p>Any projectile implementing this interface is handled with AOE logic
 * inside {@code CollisionManager.triggerImpact()} without the need for
 * {@code instanceof} checks against concrete classes.
 *
 * <p>Implemented by: {@link MagicSpell}, {@link EggProjectile}.
 */
public interface AoeProjectile {
    /** @return the explosion radius in world units */
    float getAoeRadius();
    /** @return base damage at the centre of the explosion */
    int   getDamage();
}
