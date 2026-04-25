package sk.stuba.fiit.projectiles;

import org.slf4j.Logger;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.ObjectPool;
import sk.stuba.fiit.util.Vector2D;

/**
 * Centralny spravca {@link ObjectPool} instancii pre vsetky typy projektilov.
 *
 * <p>Preco singleton: pool musi prezit cez cely level, ale nie cez resetGame().
 * Singleton zaisti, ze kazdy Attack dostane ten isty pool bez predavania
 * referencie cez konstruktory.
 *
 * <p>Vzor pouzitia v ArrowAttack.execute():
 * <pre>
 *   Arrow arrow = ProjectilePool.getInstance().obtainArrow();
 *   arrow.reset(damage, speed, spawnPos, direction, piercing);
 *   level.addProjectile(arrow);
 *
 *   // ked projektil skonci (v Level.update):
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

    // Predvolena (neplatna) pozicia pre factory instancie – ihned sa prepise reset()
    private static final Vector2D ZERO      = new Vector2D(0, 0);
    private static final Vector2D RIGHT     = new Vector2D(1, 0);
    private static final float    POOL_SPEED = 1f;

    private static final Logger log = GameLogger.get(ProjectilePool.class);

    private ProjectilePool() {
        arrowPool = new ObjectPool<>(
            // factory: vytvori "prazdnu" sablonu – hodnoty sa prepisu v reset()
            () -> new Arrow(0, POOL_SPEED, ZERO, RIGHT),
            // resetAction: pred opatovnym pouzitim znovu aktivujeme projektil
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
    //  Obtain – vratia pripraveny objekt s resetovanym stavom
    // -------------------------------------------------------------------------

    /**
     * Vrati Arrow z poolu. Volajuci musi ihned zavolat
     * {@link Arrow#reset(int, float, Vector2D, Vector2D)}
     * aby nastavil skutocne herne hodnoty.
     */
    public Arrow obtainArrow() {
        return arrowPool.obtain();
    }

    /**
     * Vrati MagicSpell z poolu. Volajuci musi ihned zavolat
     * {@link MagicSpell#reset(int, float, Vector2D, Vector2D, float)}
     * aby nastavil skutocne herne hodnoty.
     */
    public MagicSpell obtainSpell() {
        return spellPool.obtain();
    }

    /**
     * Vrati TurdflyProjectile z poolu. Volajuci musi ihned zavolat
     * {@link TurdflyProjectile#reset(Vector2D, Vector2D)}
     * aby nastavil skutocne herne hodnoty.
     */
    public TurdflyProjectile obtainTurdfly() {
        return turdflyPool.obtain();
    }

    // -------------------------------------------------------------------------
    //  Free – vratenie objektu spat do poolu
    // -------------------------------------------------------------------------

    public void free(Arrow arrow)               { arrowPool.free(arrow); }
    public void free(MagicSpell spell)          { spellPool.free(spell); }
    public void free(TurdflyProjectile turdfly) { turdflyPool.free(turdfly); }

    // -------------------------------------------------------------------------
    //  Reset – vola sa pri resetGame() aby sme zahodili stare instancie
    // -------------------------------------------------------------------------

    /**
     * Vyprazdni vsetky pooly. Volat z {@code GameManager.resetGame()}.
     */
    public void clearAll() {
        logStats();
        arrowPool.clear();
        spellPool.clear();
        turdflyPool.clear();
        log.info("ProjectilePool cleared (game reset)");
    }

    // -------------------------------------------------------------------------
    //  Diagnostika
    // -------------------------------------------------------------------------

    public void logStats() {
        log.info("ProjectilePool stats: arrows=[created={}, reused={}, ratio={}]"
                + " spells=[created={}, reused={}, ratio={}]"
                + " turdflyies=[created={}, reused={}, ratio={}]",
            arrowPool.getTotalCreated(), arrowPool.getTotalReused(), arrowPool.getReuseRatio(),
            spellPool.getTotalCreated(), spellPool.getTotalReused(), spellPool.getReuseRatio(),
            turdflyPool.getTotalCreated(), turdflyPool.getTotalReused(), turdflyPool.getReuseRatio()
        );
    }

    public ObjectPool<Arrow>             getArrowPool()   { return arrowPool; }
    public ObjectPool<MagicSpell>        getSpellPool()   { return spellPool; }
    public ObjectPool<TurdflyProjectile> getTurdflyPool() { return turdflyPool; }
}
