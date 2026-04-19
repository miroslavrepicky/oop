package sk.stuba.fiit.attacks;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;

import java.util.List;

/**
 * Decorator that adds a Damage-over-Time burn effect on top of a base spell attack.
 *
 * <p>Added behaviour compared to the wrapped {@link SpellAttack}:
 * <ul>
 *   <li>Custom animation: {@code "cast_fire"}</li>
 *   <li>Applies a {@link BurnEffect} to the nearest living enemy in the attack direction.</li>
 *   <li>Increases mana cost by {@value #EXTRA_MANA}.</li>
 * </ul>
 *
 * <p>The burn effect is ticked every frame by {@code Level.update()} and
 * removed automatically once expired.
 */
public class FireSpellDecorator extends AttackDecorator {

    /** Sekundy horenia po zásahu. */
    private static final float BURN_TICKS  = 3.0f;
    /** Poškodenie za sekundu počas horenia. */
    private static final int   BURN_DPS    = 8;
    /** Príplatok k mana costu základného útoku. */
    private static final int   EXTRA_MANA  = 15;

    private static final Logger log = GameLogger.get(FireSpellDecorator.class);

    public FireSpellDecorator(Attack wrapped) {
        super(wrapped);
    }

    // -------------------------------------------------------------------------
    //  execute: zavolá pôvodný spawn projektilu + aplikuje burn
    // -------------------------------------------------------------------------

    @Override
    public Projectile execute(Character attacker, Level level) {
        Projectile p = wrapped.execute(attacker, level);
        if (p != null) {
            p.setEffectFactory(target -> new BurnEffect(target, BURN_TICKS, BURN_DPS));
        }
        return p;
    }

    private void applyBurnToNearestEnemy(PlayerCharacter attacker, Level level) {
        float ax = attacker.getPosition().getX();
        boolean right = attacker.isFacingRight();

        EnemyCharacter nearest = null;
        float minDist = Float.MAX_VALUE;

        for (EnemyCharacter enemy : level.getEnemies()) {
            if (!enemy.isAlive()) continue;
            float ex = enemy.getPosition().getX();
            float dist = (ex - ax) * (right ? 1f : -1f);
            if (dist >= 0 && dist < minDist) {
                minDist = dist;
                nearest = enemy;
            }
        }

        if (nearest != null) {
            // Pridáme burn efekt – tick-uje v Level.update() cez BurnEffect
            level.addStatusEffect(new BurnEffect(nearest, BURN_TICKS, BURN_DPS));
        }
    }

    // -------------------------------------------------------------------------
    //  Animácia – fire má vlastnú animáciu v atlase
    // -------------------------------------------------------------------------

    /**
     * Vráti "cast_fire" ak animácia existuje; inak fallback na wrapped animáciu.
     * Toto je kľúčový rozdiel oproti zavolaniu super.getAnimationName():
     * každý dekorátor môže mať svoju unikátnu animáciu bez zmeny SpellAttack.
     */
    @Override
    public String getAnimationName() {
        return "cast_fire";
    }

    @Override
    public float getAnimationDuration(AnimationManager am) {
        // Ak atlas má "cast_fire", použijeme jeho dĺžku; inak fallback
        if (am != null && am.hasAnimation("cast_fire")) {
            return am.getAnimationDuration("cast_fire");
        }
        return wrapped.getAnimationDuration(am);
    }

    /** Fire kúzlo stojí viac many. */
    @Override
    public int getManaCost() {
        return wrapped.getManaCost() + EXTRA_MANA;
    }

    // =========================================================================
    //  Vnorená trieda: BurnEffect
    //  Spravuje DoT tick pre jednu postavu. Level ju drží v zozname
    //  a každý frame volá tick(deltaTime).
    // =========================================================================

    /**
     * DoT efekt aplikovaný na konkrétneho nepriateľa.
     * Level volá {@link #tick(float)} každý frame; po vypršaní je {@link #isExpired()} true.
     */
    public static class BurnEffect implements StatusEffect {

        private final Character target;
        private final int            dps;
        private float                remainingTime;
        private float                damageAccumulator = 0f;

        public BurnEffect(Character target, float duration, int dps) {
            this.target        = target;
            this.dps           = dps;
            this.remainingTime = duration;
        }

        /**
         * Volá sa každý frame z Level.update().
         * Poškodenie sa akumuluje a aplikuje po celých jednotkách,
         * aby nedochádzalo k strate poškodenia zaokrúhľovaním.
         */
        public void tick(float deltaTime) {
            if (isExpired() || !target.isAlive()) return;

            remainingTime     -= deltaTime;
            damageAccumulator += dps * deltaTime;

            int dmg = (int) damageAccumulator;
            if (dmg > 0) {
                target.takeDamage(dmg);
                damageAccumulator -= dmg;
                if (log.isDebugEnabled()) {
                    log.debug("Burn tick: target={}, dmg={}, remaining={:.1f}s",
                        target.getName(), dmg, remainingTime);
                }
            }
        }

        public boolean isExpired() {
            return remainingTime <= 0f || !target.isAlive();
        }

        public Character getTarget() { return target; }
    }
}
