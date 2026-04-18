package sk.stuba.fiit.projectiles;

import org.slf4j.Logger;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.ObjectPool;
import sk.stuba.fiit.util.Vector2D;

/**
 * Centrálny správca {@link ObjectPool} inštancií pre všetky typy projektilov.
 *
 * <p>Prečo singleton: pool musí prežiť cez celý level, ale nie cez resetGame().
 * Singleton zaistí, že každý Attack dostane ten istý pool bez predávania
 * referencie cez konštruktory.
 *
 * <p>Vzor použitia v ArrowAttack.execute():
 * <pre>
 *   Arrow arrow = ProjectilePool.getInstance().obtainArrow();
 *   arrow.reset(damage, speed, spawnPos, direction, piercing);
 *   level.addProjectile(arrow);
 *
 *   // keď projektil skončí (v Level.update):
 *   if (!p.isActive()) {
 *       ProjectilePool.getInstance().free(p);
 *   }
 * </pre>
 */
public final class ProjectilePool {

    private static ProjectilePool instance;

    private final ObjectPool<Arrow> arrowPool;
    private final ObjectPool<MagicSpell>      spellPool;
    private final ObjectPool<TurdflyProjectile> turdflyPool;

    // Predvolená (neplatná) pozícia pre factory inštancie – ihneď sa prepíše reset()
    private static final Vector2D ZERO      = new Vector2D(0, 0);
    private static final Vector2D RIGHT     = new Vector2D(1, 0);
    private static final float    POOL_SPEED = 1f;

    private static final Logger log = GameLogger.get(ProjectilePool.class);

    private ProjectilePool() {
        arrowPool = new ObjectPool<>(
            // factory: vytvorí "prázdnu" šablónu – hodnoty sa prepíšu v reset()
            () -> new Arrow(0, POOL_SPEED, ZERO, RIGHT, false),
            // resetAction: pred opätovným použitím znovu aktivujeme projektil
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
    //  Obtain – vrátia pripravený objekt s resetovaným stavom
    // -------------------------------------------------------------------------

    /**
     * Vrátí Arrow z poolu. Volajúci musí ihneď zavolať
     * {@link Arrow#reset(int, float, Vector2D, Vector2D, boolean)}
     * aby nastavil skutočné herné hodnoty.
     */
    public Arrow obtainArrow() {
        return arrowPool.obtain();
    }

    /**
     * Vrátí MagicSpell z poolu. Volajúci musí ihneď zavolať
     * {@link MagicSpell#reset(int, float, Vector2D, Vector2D, float)}
     * aby nastavil skutočné herné hodnoty.
     */
    public MagicSpell obtainSpell() {
        return spellPool.obtain();
    }

    /**
     * Vrátí TurdflyProjectile z poolu. Volajúci musí ihneď zavolať
     * {@link TurdflyProjectile#reset(Vector2D, Vector2D)}
     * aby nastavil skutočné herné hodnoty.
     */
    public TurdflyProjectile obtainTurdfly() {
        return turdflyPool.obtain();
    }

    // -------------------------------------------------------------------------
    //  Free – vrátenie objektu späť do poolu
    // -------------------------------------------------------------------------

    public void free(Arrow arrow)               { arrowPool.free(arrow); }
    public void free(MagicSpell spell)          { spellPool.free(spell); }
    public void free(TurdflyProjectile turdfly) { turdflyPool.free(turdfly); }

    // -------------------------------------------------------------------------
    //  Reset – volá sa pri resetGame() aby sme zahodili staré inštancie
    // -------------------------------------------------------------------------

    /**
     * Vyprázdni všetky pooly. Volať z {@code GameManager.resetGame()}.
     */
    public void clearAll() {
        arrowPool.clear();
        spellPool.clear();
        turdflyPool.clear();
        log.info("ProjectilePool cleared (game reset)");
    }

    // -------------------------------------------------------------------------
    //  Diagnostika
    // -------------------------------------------------------------------------

    public void logStats() {
        log.info("ProjectilePool stats: arrows=[created={}, reused={}, ratio={:.2f}]"
                + " spells=[created={}, reused={}, ratio={:.2f}]"
                + " turdflyies=[created={}, reused={}, ratio={:.2f}]",
            arrowPool.getTotalCreated(), arrowPool.getTotalReused(), arrowPool.getReuseRatio(),
            spellPool.getTotalCreated(), spellPool.getTotalReused(), spellPool.getReuseRatio(),
            turdflyPool.getTotalCreated(), turdflyPool.getTotalReused(), turdflyPool.getReuseRatio()
        );
    }

    public ObjectPool<Arrow>             getArrowPool()   { return arrowPool; }
    public ObjectPool<MagicSpell>        getSpellPool()   { return spellPool; }
    public ObjectPool<TurdflyProjectile> getTurdflyPool() { return turdflyPool; }
}
