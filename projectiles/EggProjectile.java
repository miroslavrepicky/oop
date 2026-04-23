package sk.stuba.fiit.projectiles;

import org.slf4j.Logger;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.Renderable;
import sk.stuba.fiit.util.Vector2D;

/**
 * A stationary bomb-style projectile spawned when a duck is killed (50 % chance).
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link EggState#TICKING} – plays the BOMB animation for {@value #BOMB_DURATION} seconds.</li>
 *   <li>{@link EggState#BLASTING} – plays the BLAST animation; {@code CollisionManager}
 *       applies AOE damage exactly once (guarded by {@link #isDamageDealt()}).</li>
 *   <li>After the blast animation ends, {@code active} is set to {@code false}.</li>
 * </ol>
 *
 * <p>Implements {@link Renderable}: render dimensions and offsets change depending
 * on the current state so {@code GameRenderer} does not need to know the state.
 */
public class EggProjectile extends Projectile implements AoeProjectile, Renderable {
    private static final Logger log = GameLogger.get(EggProjectile.class);
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
        log.info("EggProjectile spawned: pos=({},{}), fuseTime={}s",
            String.format("%.1f", position.getX()),
            String.format("%.1f", position.getY()),
            BOMB_DURATION);
    }

    /**
     * Advances the fuse/blast timer and handles state transitions.
     * Damage at explosion time is {@code CollisionManager}'s responsibility.
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
                    log.info("EggProjectile state: TICKING → BLASTING, pos=({},{})",
                        String.format("%.1f", position.getX()),
                        String.format("%.1f", position.getY()));
                }
                break;
            case BLASTING:
                if (stateTimer <= 0f) {
                    active = false;
                    if (log.isDebugEnabled()) {
                        log.debug("EggProjectile deactivated after blast: pos=({},{})",
                            String.format("%.1f", position.getX()),
                            String.format("%.1f", position.getY()));
                    }
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
