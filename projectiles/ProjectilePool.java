package sk.stuba.fiit.projectiles;

import org.slf4j.Logger;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.ObjectPool;
import sk.stuba.fiit.util.Vector2D;

/**
 * Central manager for {@link ObjectPool} instances covering all pooled projectile types.
 *
 * <p>Projectiles are created and destroyed many times per second; allocating a
 * {@code new Arrow(…)} on every shot puts pressure on the garbage collector.
 * This singleton recycles instances from fixed-size pools instead of allocating
 * new objects, keeping GC pauses short during gameplay.
 *
 * <h2>Lifecycle</h2>
 * <p>The pool must outlive an entire level but must be reset between game sessions.
 * Call {@link #clearAll()} from {@code GameManager.resetGame()} to discard stale
 * instances before starting a new game.
 *
 * <h2>Usage pattern</h2>
 * <pre>
 *   // Obtain a pre-warmed instance
 *   Arrow arrow = ProjectilePool.getInstance().obtainArrow();
 *   // Reinitialise with real game values
 *   arrow.reset(damage, speed, spawnPos, direction);
 *   level.addProjectile(arrow);
 *
 *   // When the projectile becomes inactive (Level.update):
 *   if (!p.isActive() &amp;&amp; p instanceof Poolable) {
 *       ((Poolable) p).returnToPool();
 *   }
 * </pre>
 */
public final class ProjectilePool {

    private static ProjectilePool instance;

    private final ObjectPool<Arrow> arrowPool;
    private final ObjectPool<MagicSpell>      spellPool;
    private final ObjectPool<TurdflyProjectile> turdflyPool;

    /** Placeholder position used for factory instances; overwritten immediately by {@code reset()}. */
    private static final Vector2D ZERO      = new Vector2D(0, 0);
    private static final Vector2D RIGHT     = new Vector2D(1, 0);
    private static final float    POOL_SPEED = 1f;

    private static final Logger log = GameLogger.get(ProjectilePool.class);

    private ProjectilePool() {
        arrowPool = new ObjectPool<>(
            // factory: vytvori "prazdnu" sablonu – hodnoty sa prepisu v reset()
            () -> new Arrow(0, POOL_SPEED, ZERO, RIGHT),
            arrow -> arrow.setActive(true),
            /* maxSize */ 30
        );

        spellPool = new ObjectPool<>(
            () -> new MagicSpell(0, POOL_SPEED, ZERO, RIGHT, 100f),
            spell -> spell.setActive(true),
            /* maxSize */ 15
        );

        turdflyPool = new ObjectPool<>(
            () -> new TurdflyProjectile(ZERO, RIGHT),
            turdfly -> turdfly.setActive(true),
            /* maxSize */ 10
        );

        log.info("ProjectilePool initialised");
    }

    public static ProjectilePool getInstance() {
        if (instance == null) {
            instance = new ProjectilePool();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    //  Obtain – return a pre-warmed object ready for use
    // -------------------------------------------------------------------------

    /**
     * Returns an {@link Arrow} from the pool.
     * The caller must immediately call {@link Arrow#reset(int, float, Vector2D, Vector2D)}
     * to configure real game values before adding it to the level.
     *
     * @return a pooled (or freshly created) {@link Arrow} instance
     */
    public Arrow obtainArrow() {
        return arrowPool.obtain();
    }

    /**
     * Returns a {@link MagicSpell} from the pool.
     * The caller must immediately call
     * {@link MagicSpell#reset(int, float, Vector2D, Vector2D, float)}
     * to configure real game values before adding it to the level.
     *
     * @return a pooled (or freshly created) {@link MagicSpell} instance
     */
    public MagicSpell obtainSpell() {
        return spellPool.obtain();
    }

    /**
     * Returns a {@link TurdflyProjectile} from the pool.
     * The caller must immediately call {@link TurdflyProjectile#reset(Vector2D, Vector2D)}
     * to configure real game values before adding it to the level.
     *
     * @return a pooled (or freshly created) {@link TurdflyProjectile} instance
     */
    public TurdflyProjectile obtainTurdfly() {
        return turdflyPool.obtain();
    }

    // -------------------------------------------------------------------------
    //  Free – return an object back to the pool
    // -------------------------------------------------------------------------

    /**
     * Returns an {@link Arrow} to the pool for future reuse.
     * The caller must not use the object after this call.
     *
     * @param arrow the arrow to recycle
     */
    public void free(Arrow arrow)               { arrowPool.free(arrow); }

    /**
     * Returns a {@link MagicSpell} to the pool for future reuse.
     * The caller must not use the object after this call.
     *
     * @param spell the spell to recycle
     */
    public void free(MagicSpell spell)          { spellPool.free(spell); }

    /**
     * Returns a {@link TurdflyProjectile} to the pool for future reuse.
     * The caller must not use the object after this call.
     *
     * @param turdfly the projectile to recycle
     */
    public void free(TurdflyProjectile turdfly) { turdflyPool.free(turdfly); }

    // -------------------------------------------------------------------------
    //  Reset – called from GameManager.resetGame()
    // -------------------------------------------------------------------------

    /**
     * Empties all three pools, discarding their cached instances.
     * Must be called from {@code GameManager.resetGame()} before starting a new game
     * so that stale projectile references do not leak across sessions.
     */
    public void clearAll() {
        logStats();
        arrowPool.clear();
        spellPool.clear();
        turdflyPool.clear();
        log.info("ProjectilePool cleared (game reset)");
    }

    // -------------------------------------------------------------------------
    //  Diagnostics
    // -------------------------------------------------------------------------

    /**
     * Logs pool statistics (total created, reused, reuse ratio) for all three
     * projectile types at INFO level. Called automatically by {@link #clearAll()}.
     */
    public void logStats() {
        log.info("ProjectilePool stats: arrows=[created={}, reused={}, ratio={}]"
                + " spells=[created={}, reused={}, ratio={}]"
                + " turdflyies=[created={}, reused={}, ratio={}]",
            arrowPool.getTotalCreated(), arrowPool.getTotalReused(), arrowPool.getReuseRatio(),
            spellPool.getTotalCreated(), spellPool.getTotalReused(), spellPool.getReuseRatio(),
            turdflyPool.getTotalCreated(), turdflyPool.getTotalReused(), turdflyPool.getReuseRatio()
        );
    }
}
