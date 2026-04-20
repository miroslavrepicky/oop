package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import sk.stuba.fiit.attacks.*;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.NormalGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class DarkKnight extends EnemyCharacter {
    private static final int ARMOR = 30; // silný boss – vysoké brnenie
    private AnimationManager animationManager;

    private int phase;
    private float specialCooldown;
    private static final int MAX_PHASES = 3;
    private static final float COOLDOWN_MAX = 5.0f;
    List<Attack> meleeAttacks = new ArrayList<>();
    List<Attack> spellAttacks = new ArrayList<>();

    public DarkKnight(Vector2D position) {
        super("DarkKnight", 500, 50, 2.0f, position, 200f, 400f, ARMOR, ARMOR);
        this.phase = 1;
        this.specialCooldown = 0f;
        this.gravityStrategy = new NormalGravity();
        initAnimations();
        Vector2D idleSize = animationManager.getFirstFrameSize("idle");
        this.hitbox.setSize(idleSize.getX(), idleSize.getY());
        meleeAttacks.add(new MeleeAttack(64));
        meleeAttacks.add(new MeleeAttack(32));
        spellAttacks.add(new SpellAttack(5.0f, 0, 0));
        spellAttacks.add(new FireDecorator(new SpellAttack(5.0f, 0, 0)));
        spellAttacks.add(new FreezeDecorator(new SpellAttack(5.0f, 0, 0)));
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/dark_knight/dark_knight.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("jump",   "JUMP/JUMP",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("cast", "PRAY/PRAY", 0.07f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.3f, Animation.PlayMode.NORMAL);
    }

    @Override
    public void triggerAttack() {
        super.triggerAttack();
    }

    public void switchPhase() {
        if (phase < MAX_PHASES) {
            phase++;
        }
    }

    @Override
    public void update(UpdateContext ctx) {
        // prepnutie fázy podľa HP
        if (phase == 1 && hp < maxHp * 0.66f) switchPhase();
        if (phase == 2 && hp < maxHp * 0.33f) switchPhase();

        if (specialCooldown > 0) specialCooldown -= ctx.deltaTime;
    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public int getPhase() { return phase; }
}
