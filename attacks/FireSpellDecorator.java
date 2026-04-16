package sk.stuba.fiit.attacks;

import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.world.Level;

/**
 * Dekorátor: ohnivé kúzlo.
 *
 * Čo pridáva oproti wrapped SpellAttack:
 *  - vlastná animácia "cast_fire" (iná ako štandardné "cast")
 *  - DoT (Damage over Time) efekt – zasiahnutý nepriateľ horí
 *    {@link #BURN_TICKS} sekúnd a stráca {@link #BURN_DPS} HP/s
 *  - zvýšený mana cost
 *
 * Čo nemeníme (delegate na wrapped):
 *  - logika spawnovania projektilu (SpellAttack.execute)
 *  - animácia trvanie (závisí od atlasu)
 *
 * Burn efekt: Level.update() zavolá BurnEffect.tick() každý frame.
 * BurnEffect je vnorená trieda – nepotrebuje vlastný súbor.
 */
public class FireSpellDecorator extends AttackDecorator {

    /** Sekundy horenia po zásahu. */
    private static final float BURN_TICKS  = 3.0f;
    /** Poškodenie za sekundu počas horenia. */
    private static final int   BURN_DPS    = 8;
    /** Príplatok k mana costu základného útoku. */
    private static final int   EXTRA_MANA  = 15;

    public FireSpellDecorator(Attack wrapped) {
        super(wrapped);
    }

    // -------------------------------------------------------------------------
    //  execute: zavolá pôvodný spawn projektilu + aplikuje burn
    // -------------------------------------------------------------------------

    @Override
    public void execute(Character attacker, Level level) {
        // 1. Pôvodný SpellAttack vypustí MagicSpell projektil
        wrapped.execute(attacker, level);

        // 2. Burn efekt na prvého nepriateľa v dosahu
        //    (pre zjednodušenie hľadáme najbližšieho živého)
        if (attacker instanceof PlayerCharacter) {
            applyBurnToNearestEnemy((PlayerCharacter) attacker, level);
        }
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

        private final EnemyCharacter target;
        private final int            dps;
        private float                remainingTime;
        private float                damageAccumulator = 0f;

        public BurnEffect(EnemyCharacter target, float duration, int dps) {
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
            }
        }

        public boolean isExpired() {
            return remainingTime <= 0f || !target.isAlive();
        }

        public EnemyCharacter getTarget() { return target; }
    }
}
