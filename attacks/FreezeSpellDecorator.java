package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

import java.util.List;

/**
 * Decorator that adds a slow/freeze status effect on top of a base spell attack.
 *
 * <p>Added behavior compared to the wrapped {@link SpellAttack}:
 * <ul>
 *   <li>Custom animation: {@code "cast_freeze"}</li>
 *   <li>Applies a {@link FreezeEffect} that reduces the nearest enemy's speed
 *       to {@value #SLOW_FACTOR} × original for {@value #FREEZE_DURATION} seconds.</li>
 *   <li>Reduces mana cost by {@value #DISCOUNT_MANA} compared to the wrapped attack.</li>
 * </ul>
 *
 * <p>Stacking: if a freeze is applied to an already-frozen target, the existing
 * effect is replaced (handled in {@code Level.addStatusEffect()}) to avoid
 * double speed restoration.
 */
public class FreezeSpellDecorator extends AttackDecorator {

    /** Percento pôvodnej rýchlosti počas freeze (0.3 = 30 %). */
    private static final float SLOW_FACTOR      = 0.3f;
    /** Sekúnd trvania spomalenia. */
    private static final float FREEZE_DURATION  = 2.5f;
    /** Zníženie mana costu oproti základu. */
    private static final int   DISCOUNT_MANA    = 5;

    private static final Logger log = GameLogger.get(FireSpellDecorator.class);

    public FreezeSpellDecorator(Attack wrapped) {
        super(wrapped);
    }

    @Override
    public Projectile execute(Character attacker, Level level) {
        Projectile p = wrapped.execute(attacker, level);
        if (p != null) {
            p.setEffectFactory(target -> new FreezeEffect(target, FREEZE_DURATION, SLOW_FACTOR));
        }
        return p;
    }

    private void applyFreezeToNearestEnemy(PlayerCharacter attacker, Level level) {
        float ax    = attacker.getPosition().getX();
        boolean right = attacker.isFacingRight();

        EnemyCharacter nearest = null;
        float minDist = Float.MAX_VALUE;

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            float ex   = enemy.getPosition().getX();
            float dist = (ex - ax) * (right ? 1f : -1f);
            if (dist >= 0 && dist < minDist) {
                minDist = dist;
                nearest = enemy;
            }
        }

        if (nearest != null) {
            level.addStatusEffect(new FreezeEffect(nearest, FREEZE_DURATION, SLOW_FACTOR));
        }
    }

    // -------------------------------------------------------------------------
    //  Animácia – freeze má vlastnú animáciu
    // -------------------------------------------------------------------------

    @Override
    public String getAnimationName() {
        return "cast_freeze";
    }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        if (am != null && am.hasAnimation("cast_freeze")) {
            return am.getAnimationDuration("cast_freeze");
        }
        return wrapped.getAnimationDuration(am);
    }

    @Override
    public int getManaCost() {
        return Math.max(0, wrapped.getManaCost() - DISCOUNT_MANA);
    }

    // =========================================================================
    //  Vnorená trieda: FreezeEffect
    // =========================================================================

    /**
     * Spomaľovací efekt. Upraví rýchlosť cieľa na (original × slowFactor)
     * a po expirácii ju obnoví.
     *
     * Stacking: ak sa zavolá znova na ten istý cieľ, Level si vytvorí
     * nový FreezeEffect. Odporúča sa pred pridaním skontrolovať existenciu
     * (Level.hasStatusEffect) a nahradiť ho, nie stackovať – inak by
     * obnovenie rýchlosti prebehlo dvakrát.
     */
    public static class FreezeEffect implements StatusEffect {

        private final                Character target;
        private final float          originalSpeed;
        private final float          slowFactor;
        private float                remainingTime;
        private boolean              applied = false;

        public FreezeEffect(Character target, float duration, float slowFactor) {
            this.target        = target;
            this.slowFactor    = slowFactor;
            this.remainingTime = duration;
            this.originalSpeed = target.getSpeed();
        }

        /**
         * Volá sa každý frame z Level.update().
         * Prvé volanie aplikuje spomalenie; po expirácii obnoví rýchlosť.
         */
        //TODO maybe bugged... restoreSpeed() is never called
        public void tick(float deltaTime) {
            if (!applied) {
                applyFreeze();
            }

            if (isExpired()) return;

            remainingTime -= deltaTime;

            if (remainingTime <= 0f) {
                restoreSpeed();
            }
        }

        private void applyFreeze() {
            target.setSpeed(originalSpeed * slowFactor);
            applied = true;
            log.info("Freeze applied: target={}, speedReduced={}->{}",
                target.getName(),
                String.format("%.1f", originalSpeed),
                String.format("%.1f", originalSpeed * slowFactor));
        }

        private void restoreSpeed() {
            target.setSpeed(originalSpeed);
            log.info("Freeze expired: target={}, speedRestored={}",
                target.getName(),
                String.format("%.1f", originalSpeed));
        }

        public boolean isExpired() {
            return remainingTime <= 0f || !target.isAlive();
        }

        public Character getTarget() { return target; }
    }
}
