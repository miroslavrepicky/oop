package sk.stuba.fiit.projectiles;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.util.Vector2D;

/**
 * Vajce ktoré sa spawne priamo na zemi po zabití kačky.
 * Odpočítava (BOMB animácia) a potom vybuchne (BLAST animácia).
 *
 * Životný cyklus:
 *   TICKING  → BOMB animácia (BOMB_DURATION sekúnd)
 *   BLASTING → BLAST animácia, potom active = false
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

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/egg/egg.atlas");
        animationManager.addAnimation("bomb",  "BOMB/BOMB",   0.25f);
        animationManager.addAnimation("blast", "BLAST/BLAST", 0.08f);
        animationManager.play("bomb");
    }

    @Override
    public void update(float deltaTime) {
        stateTimer -= deltaTime;
        animationManager.update(deltaTime);

        switch (eggState) {
            case TICKING:
                if (stateTimer <= 0f) {
                    eggState   = EggState.BLASTING;
                    stateTimer = BLAST_DURATION;
                    animationManager.play("blast");
                    dealAoeDamage();
                }
                break;

            case BLASTING:
                if (stateTimer <= 0f) {
                    active = false;
                }
                break;
        }
    }

    private void dealAoeDamage() {
        if (damageDealt) return;
        damageDealt = true;

        PlayerCharacter player = GameManager.getInstance().getInventory().getActive();
        if (player == null) return;

        double dist = position.distanceTo(player.getPosition());
        if (dist <= AOE_RADIUS) {
            float falloff = 1f - (float)(dist / AOE_RADIUS);
            int dmg = Math.max(1, (int)(BLAST_DAMAGE * falloff));
            player.takeDamage(dmg);
            System.out.println("Vajce vybuchlo! Hráč dostal " + dmg + " poškodenia.");
        }
    }

    @Override
    public void onCollision(Object other) {
        // vajce nereaguje na kolízie – iba AoE pri výbuchu
    }

    public EggState         getEggState()         { return eggState; }
    public AnimationManager getAnimationManager() { return animationManager; }
    public float            getAoeRadius()        { return AOE_RADIUS; }
}
