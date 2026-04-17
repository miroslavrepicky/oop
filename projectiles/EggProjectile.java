package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * Vajce ktoré sa spawnuje priamo na zemi po zabití kačky.
 * Odpočítava (BOMB animácia) a potom vybuchne (BLAST animácia).
 *
 * Životný cyklus:
 *   TICKING  → BOMB animácia (BOMB_DURATION sekúnd)
 *   BLASTING → BLAST animácia, potom active = false
 *
 * Implementuje {@link Renderable} – vizuálne parametre (veľkosť, offset)
 * sa menia podľa aktuálneho stavu; GameRenderer o tom nemusí vedieť.
 */
public class EggProjectile extends Projectile implements AoeProjectile, Renderable {

    public enum EggState { TICKING, BLASTING }

    private static final float BOMB_DURATION  = 2.5f;
    private static final float BLAST_DURATION = 0.8f;
    private static final float AOE_RADIUS     = 80f;
    private static final int   BLAST_DAMAGE   = 30;

    // Rozmery v závislosti od stavu
    private static final float BOMB_W  = 32f;
    private static final float BOMB_H  = 32f;
    private static final float BLAST_W = 64f;
    private static final float BLAST_H = 64f;

    private EggState eggState   = EggState.TICKING;
    private float    stateTimer = BOMB_DURATION;
    private boolean  damageDealt = false;

    private final AnimationManager animationManager;

    public EggProjectile(Vector2D position) {
        super(BLAST_DAMAGE, 0f, position, new Vector2D(0, 0));
        animationManager = new AnimationManager("atlas/egg/egg.atlas");
        animationManager.addAnimation("bomb",  "BOMB/BOMB",   0.25f);
        animationManager.addAnimation("blast", "BLAST/BLAST", 0.08f);
        animationManager.play("bomb");
    }

    /**
     * Riadi časovač a prechody stavov.
     * Damage pri výbuchu je zodpovednosť CollisionManageru –
     * tu iba meníme stav a animáciu.
     */
    @Override
    public void update(UpdateContext ctx) {
        stateTimer -= ctx.deltaTime;
        animationManager.update(ctx.deltaTime);

        switch (eggState) {
            case TICKING:
                if (stateTimer <= 0f) {
                    eggState   = EggState.BLASTING;
                    stateTimer = BLAST_DURATION;
                    animationManager.play("blast");
                }
                break;
            case BLASTING:
                if (stateTimer <= 0f) {
                    active = false;
                }
                break;
        }
    }

    /** EggProjectile je stacionárny – kolízie so stenami ho nezaujímajú. */
    @Override
    public void onCollision(Object other) {}

    // -------------------------------------------------------------------------
    //  AoeProjectile
    // -------------------------------------------------------------------------

    @Override public float getAoeRadius() { return AOE_RADIUS; }
    @Override public int   getDamage()    { return damage; }

    // -------------------------------------------------------------------------
    //  Renderable – rozmery a offset sa menia podľa stavu
    // -------------------------------------------------------------------------

    @Override public AnimationManager getAnimationManager() { return animationManager; }
    @Override public boolean isFlippedX()    { return false; }

    @Override
    public float getRenderWidth() {
        return eggState == EggState.BLASTING ? BLAST_W : BOMB_W;
    }

    @Override
    public float getRenderHeight() {
        return eggState == EggState.BLASTING ? BLAST_H : BOMB_H;
    }

    @Override
    public float getRenderOffsetX() {
        return eggState == EggState.BLASTING ? -16f : 0f;
    }

    @Override
    public float getRenderOffsetY() {
        return eggState == EggState.BLASTING ? -16f : 0f;
    }

    // -------------------------------------------------------------------------
    //  API pre CollisionManager
    // -------------------------------------------------------------------------

    public boolean  isDamageDealt() { return damageDealt; }
    public void     markDamageDealt() { damageDealt = true; }
    public EggState getEggState()   { return eggState; }
}
