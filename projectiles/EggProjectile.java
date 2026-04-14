package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.util.Vector2D;


/**
 * Vajce ktore sa spawne priamo na zemi po zabiti kacky.
 * Odpocitava (BOMB animacia) a potom vybuchne (BLAST animacia).
 *
 * zivotny cyklus:
 *   TICKING  -> BOMB animacia (BOMB_DURATION sekund)
 *   BLASTING -> BLAST animacia, potom active = false
 */
public class EggProjectile extends Projectile {

    public enum EggState { TICKING, BLASTING }

    private static final float BOMB_DURATION  = 2.5f;
    private static final float BLAST_DURATION = 0.8f;
    private static final float AOE_RADIUS     = 80f;
    private static final int   BLAST_DAMAGE   = 30;

    private EggState eggState    = EggState.TICKING;
    private float    stateTimer  = BOMB_DURATION;
    private boolean  damageDealt = false;

    private AnimationManager animationManager;

    public EggProjectile(Vector2D position) {
        super(BLAST_DAMAGE, 0f, position, new Vector2D(0, 0));
        initAnimations();
    }

    protected void initAnimations() {
        animationManager = new AnimationManager("atlas/egg/egg.atlas");
        animationManager.addAnimation("bomb",  "BOMB/BOMB",   0.25f);
        animationManager.addAnimation("blast", "BLAST/BLAST", 0.08f);
        animationManager.play("bomb");
    }

    /**
     * Riadi casovac a prechody stavov.
     * Damage pri vybuchu je zodpovednost CollisionManageru –
     * tu iba menime stav a animaciu.
     */
    @Override
    public void update(float deltaTime) {
        stateTimer -= deltaTime;
        if (animationManager != null) animationManager.update(deltaTime);

        switch (eggState) {
            case TICKING:
                if (stateTimer <= 0f) {
                    eggState   = EggState.BLASTING;
                    stateTimer = BLAST_DURATION;
                    if (animationManager != null) animationManager.play("blast");
                    // damage aplikuje CollisionManager ked zbada prechod do BLASTING
                }
                break;

            case BLASTING:
                if (stateTimer <= 0f) {
                    active = false;
                }
                break;
        }
    }

    /** EggProjectile je stacionarny – kolizie so stenami ho nezaujimaju. */
    @Override
    public void onCollision(Object other) {}

    // -------------------------------------------------------------------------
    //  API pre CollisionManager
    // -------------------------------------------------------------------------

    /** @return true ak CollisionManager uz aplikoval AOE damage pri tomto vybuchu. */
    public boolean isDamageDealt() { return damageDealt; }

    /** Oznaci ze damage bol uz aplikovany – zabrani opakovaniu v kazdom frame. */
    public void markDamageDealt() { damageDealt = true; }

    // -------------------------------------------------------------------------
    //  Gettery
    // -------------------------------------------------------------------------

    public EggState         getEggState()         { return eggState; }
    public AnimationManager getAnimationManager() { return animationManager; }
    public float            getAoeRadius()        { return AOE_RADIUS; }

    /**
     * Exponuje damage pre CollisionManager (zdedeny field z Projectile).
     * Pouziva sa v checkEggExplosions() na vypocet falloff damage.
     */
    @Override
    public int getDamage() { return damage; }
}
