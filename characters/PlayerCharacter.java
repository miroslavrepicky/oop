package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

/**
 * Base class for all player-controlled characters.
 *
 * <p>Manages the primary and secondary attack lifecycle:
 * an attack is initiated via {@link #executeAttack(Attack)}, which starts an animation
 * timer and records the attack. The actual projectile/hit is spawned in
 * {@link #updateAnimation(float)} at the appropriate animation frame (near the end).
 *
 * <p>Mana management uses a template method: the base implementation treats mana
 * as unlimited ({@code Integer.MAX_VALUE}). {@code Wizzard} overrides
 * {@link #getMana()} and {@link #spendMana(int)} to use real mana values.
 *
 * <p>Gravity is applied by {@code PlayerController}, which passes the platform list
 * directly – the character does not need to call {@code GameManager} for physics data.
 */
public abstract class PlayerCharacter extends Character {
    protected Attack primaryAttack;
    protected Attack secondaryAttack;
    protected boolean isAttacking       = false;
    protected float   attackAnimTimer   = 0f;
    protected Attack  currentAttack     = null;
    protected boolean projectileSpawned = false;

    public PlayerCharacter(String name, int hp, int attackPower,
                           float speed, Vector2D position) {
        this(name, hp, attackPower, speed, position, 0);
    }

    public PlayerCharacter(String name, int hp, int attackPower, float speed,
                           Vector2D position, int maxArmor) {
        super(name, hp, attackPower, speed, position, 0, maxArmor);
    }

    // --- mana – default prázdna implementácia, Wizzard override-ne ---
    protected int  getMana()             { return Integer.MAX_VALUE; }
    protected void spendMana(int amount) { }

    /**
     * Attempts to execute an attack. Checks mana cost for spell attacks and
     * starts the attack animation timer.
     * Has no effect if another attack is already in progress.
     *
     * @param attack the attack strategy to execute; {@code null} is silently ignored
     */
    protected void executeAttack(Attack attack) {
        if (attack == null || isAttacking) {
            return;
        }

        int cost = attack.getManaCost();
        if (getMana() < cost) return;
        spendMana(cost);

        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        AnimationManager am = getAnimationManager();
        if (am != null) {
            isAttacking       = true;
            currentAttack     = attack;
            attackAnimTimer   = attack.getAnimationDuration(am);
            projectileSpawned = false;
        }
    }

    public void performPrimaryAttack()   { executeAttack(primaryAttack); }
    public void performSecondaryAttack() { executeAttack(secondaryAttack); }

    /**
     * Drives the attack animation: spawns the projectile at the correct frame,
     * manages the animation timer, and switches to the appropriate locomotion
     * animation (idle / walk / jump) when the attack ends.
     */
    public void updateAnimation(UpdateContext ctx) {
        if (getAnimationManager() == null) return;

        if (!isAlive()) {
            startDeathAnimation();
            updateDeathTimer(ctx.deltaTime);
            getAnimationManager().update(ctx.deltaTime);

            if (isDeathAnimationDone()) {
                if (ctx.inventory.getActive() == this && !ctx.inventory.switchToNextAlive()) {
                    // žiadna živá postava – party je porazená, nič nerob,
                    // PlayingState.update() zachytí isPartyDefeated() a prepne stav
                }
            }
            return;
        }

        // Spawn projektilu pri správnom frame útočnej animácie
        if (!projectileSpawned && currentAttack != null) {
            float frameDuration = currentAttack.getFrameDuration(getAnimationManager());
            if (attackAnimTimer <= frameDuration * 3) {
                currentAttack.execute(this, ctx.level);
                projectileSpawned = true;
            }
        }

        if (isAttacking) {
            attackAnimTimer -= ctx.deltaTime;
            if (attackAnimTimer <= 0f) isAttacking = false;
        }

        String anim;
        if (isAttacking) {
            anim = currentAttack != null ? currentAttack.getAnimationName() : "attack";
        } else if (!isOnGround()) {
            anim = hasAnimation("jump") ? "jump" : "idle";
        } else if (Math.abs(velocityX) > 0.1f) {
            anim = "walk";
        } else {
            anim = "idle";
        }

        getAnimationManager().play(anim);
        getAnimationManager().update(ctx.deltaTime);
    }

    protected boolean hasAnimation(String name) {
        return getAnimationManager() != null && getAnimationManager().hasAnimation(name);
    }

    public Inventory getInventory() {
        return GameManager.getInstance().getInventory();
    }

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    @Override
    public void onCollision(Object other) { }
}
