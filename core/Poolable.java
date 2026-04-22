package sk.stuba.fiit.core;

import sk.stuba.fiit.projectiles.*;

/**
 * Contract for projectiles managed by {@link ObjectPool} via {@link ProjectilePool}.
 *
 * <p>Replaces the series of {@code instanceof} checks that previously existed
 * in {@code Level.returnInactiveProjectilesToPool()}. Each pooled projectile
 * now knows how to return itself to the correct pool:
 * <pre>
 *   if (p instanceof Poolable) ((Poolable) p).returnToPool();
 * </pre>
 *
 * <p>Implemented by: {@link Arrow}, {@link MagicSpell}, {@link TurdflyProjectile}.
 * {@link EggProjectile} is NOT pooled and is handled by the GC.
 */
public interface Poolable {
    /**
     * Returns this object to its owning {@link ObjectPool}.
     * Called when the projectile becomes inactive, just before it is removed
     * from the level's projectile list.
     * The caller must not use the object after this call.
     */
    void returnToPool();
}
