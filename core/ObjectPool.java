package sk.stuba.fiit.core;

import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic object pool for recycling frequently created game objects.
 *
 * <p>Projectiles ({@code Arrow}, {@code MagicSpell}, {@code TurdflyProjectile})
 * are created and discarded every second – {@code new Arrow(...)} on every shot
 * puts pressure on the garbage collector. The pool recycles instances instead
 * of allocating new ones.
 *
 * <p>Why generics are meaningful here: we have {@code ObjectPool<Arrow>},
 * {@code ObjectPool<MagicSpell>}, and {@code ObjectPool<TurdflyProjectile>} –
 * three distinct instances with different types, factory functions, and reset actions.
 *
 * @param <T> the pooled object type; must be resettable via the {@code resetAction}
 */
public class ObjectPool<T> {

    private final ArrayDeque<T> pool;
    private final Supplier<T>   factory;
    private final Consumer<T>   resetAction;
    private final int           maxSize;

    private int totalCreated = 0;
    private int totalReused  = 0;

    private static final Logger log = GameLogger.get(ObjectPool.class);

    /**
     * @param factory     supplier that creates a new instance when the pool is empty
     * @param resetAction consumer that cleans the object before reuse
     * @param maxSize     maximum number of objects kept in the pool
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> resetAction, int maxSize) {
        this.factory     = factory;
        this.resetAction = resetAction;
        this.maxSize     = maxSize;
        this.pool        = new ArrayDeque<>(maxSize);
    }

    /**
     * Returns an object from the pool or creates a new one if the pool is empty.
     * The {@code resetAction} is applied before the object is returned.
     *
     * <p>Time complexity: O(1).
     *
     * @return a ready-to-use pooled object
     */
    public T obtain() {
        if (pool.isEmpty()) {
            totalCreated++;
            T obj = factory.get();
            if (log.isDebugEnabled()) {
                log.debug("ObjectPool miss – created new instance: type={}, totalCreated={}",
                    obj.getClass().getSimpleName(), totalCreated);
            }
            return obj;
        }
        totalReused++;
        T obj = pool.pop();
        resetAction.accept(obj);
        if (log.isDebugEnabled()) {
            log.debug("ObjectPool hit – reused instance: type={}, totalReused={}, poolSize={}",
                obj.getClass().getSimpleName(), totalReused, pool.size());
        }
        return obj;
    }

    /**
     * Returns an object to the pool for future reuse.
     * If the pool is already at {@code maxSize}, the object is discarded (GC).
     *
     * <p>The caller must not use the object after this call.
     *
     * <p>Time complexity: O(1).
     *
     * @param object the object to return; {@code null} is silently ignored
     */
    public void free(T object) {
        if (object == null) return;
        if (pool.size() < maxSize) {
            pool.push(object);
            if (log.isDebugEnabled()) {
                log.debug("ObjectPool freed object: type={}, poolSize={}",
                    object.getClass().getSimpleName(), pool.size());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ObjectPool full – object discarded: type={}, maxSize={}",
                    object.getClass().getSimpleName(), maxSize);
            }
        }
    }

    /**
     * Clears all objects currently held in the pool.
     * Should be called on game reset or level change.
     */
    public void clear() {
        int cleared = pool.size();
        pool.clear();
        log.info("ObjectPool cleared: type=unknown, clearedCount={}", cleared);
    }

    // -------------------------------------------------------------------------
    //  Diagnostic getters
    // -------------------------------------------------------------------------

    /** Returns the number of objects currently waiting in the pool. */
    public int getPoolSize()     { return pool.size(); }

    /** Returns the total number of instances created (pool miss count). */
    public int getTotalCreated() { return totalCreated; }

    /** Returns the total number of instances reused from the pool (pool hit count). */
    public int getTotalReused()  { return totalReused; }

    /**
     * Returns the reuse ratio: the fraction of {@code obtain()} calls satisfied
     * from the pool. A value close to {@code 1.0} means the pool is working efficiently.
     *
     * @return reuse ratio in range [0, 1]
     */
    public float getReuseRatio() {
        int total = totalCreated + totalReused;
        return total == 0 ? 0f : (float) totalReused / total;
    }
}
