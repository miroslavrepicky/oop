package sk.stuba.fiit.core;

import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generický object pool pre recykláciu herných objektov.
 *
 * <p>Problém ktorý rieši: Projektily (Arrow, MagicSpell, TurdflyProjectile)
 * sa vytvárajú a zahadzujú každú sekundu – {@code new Arrow(...)} pri každom
 * výstrele = tlak na garbage collector. Object Pool recykluje inštancie
 * namiesto vytvárania nových.
 *
 * <p>Prečo je generickosť zmysluplná: máme {@code ObjectPool<Arrow>},
 * {@code ObjectPool<MagicSpell>}, {@code ObjectPool<TurdflyProjectile>} –
 * tri rôzne inštancie s rôznymi typmi a rôznymi factory/reset funkciami.
 *
 * <p>Prečo ho nemožno nahradiť z JDK: {@code ArrayDeque<T>} nepozná ako
 * vytvoriť nové objekty, ako ich resetovať pred opätovným použitím,
 * ani nemonitoruje využitie poolu.
 *
 * @param <T> typ objektu v poole – musí byť resetovateľný cez resetAction
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
     * @param factory     funkcia ktorá vytvorí novú inštanciu keď je pool prázdny
     * @param resetAction funkcia ktorá objekt "vyčistí" pred opätovným použitím
     * @param maxSize     maximálny počet objektov držaných v poole
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> resetAction, int maxSize) {
        this.factory     = factory;
        this.resetAction = resetAction;
        this.maxSize     = maxSize;
        this.pool        = new ArrayDeque<>(maxSize);
    }

    /**
     * Vráti objekt z poolu alebo vytvorí nový ak je pool prázdny.
     * Pred vrátením sa na objekt zavolá {@code resetAction}.
     *
     * <p>Volanie je O(1).
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
     * Vracia objekt späť do poolu na ďalšie použitie.
     * Ak je pool plný (size >= maxSize), objekt sa zahodí (GC ho zoberie).
     *
     * <p>Volanie je O(1).
     *
     * @param object objekt ktorý sa vracia – po tomto volaní ho volajúci
     *               nesmie používať (môže byť okamžite recyklovaný)
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
     * Vyprázdni pool – zahodí všetky držané inštancie.
     * Vhodné pri reset hry alebo zmene levelu.
     */
    public void clear() {
        int cleared = pool.size();
        pool.clear();
        log.info("ObjectPool cleared: type=unknown, clearedCount={}", cleared);
    }

    // -------------------------------------------------------------------------
    //  Diagnostické gettery – užitočné pre debug/logging
    // -------------------------------------------------------------------------

    /** Počet objektov momentálne čakajúcich v poole. */
    public int getPoolSize()     { return pool.size(); }

    /** Celkový počet vytvorených inštancií (pool miss). */
    public int getTotalCreated() { return totalCreated; }

    /** Celkový počet recyklovaných inštancií (pool hit). */
    public int getTotalReused()  { return totalReused; }

    /**
     * Pomer recyklácie – koľko percent obtain() volaní bolo uspokojených
     * z poolu. Hodnota blízka 1.0 = pool pracuje efektívne.
     */
    public float getReuseRatio() {
        int total = totalCreated + totalReused;
        return total == 0 ? 0f : (float) totalReused / total;
    }
}
