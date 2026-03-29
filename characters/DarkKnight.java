package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.MagicSpell;
import sk.stuba.fiit.util.Vector2D;

public class DarkKnight extends EnemyCharacter {
    private static final int ARMOR = 30; // silny boss – vysoke brnenie

    private int phase;
    private float specialCooldown;
    private static final int MAX_PHASES = 3;
    private static final float COOLDOWN_MAX = 5.0f;

    public DarkKnight(Vector2D position) {
        super("DarkKnight", 500, 50, 2.0f, position, 200f, 400f, ARMOR, ARMOR);
        this.phase = 1;
        this.specialCooldown = 0f;
        this.gravityStrategy = new NormalGravity();
    }

    @Override
    public void performAttack() {
        if (phase == 1) {
            meleeAttack();
        } else if (phase == 2) {
            meleeAttack();
            if (specialCooldown <= 0) castSpell();
        } else {
            castSpell();
        }
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    private void meleeAttack() {
        // silny melee utok
    }

    public MagicSpell castSpell() {
        specialCooldown = COOLDOWN_MAX;
        Vector2D direction = new Vector2D(-1, 0);
        return new MagicSpell(attackPower * 2, 6.0f, position, direction, 100.0f);
    }

    public void switchPhase() {
        if (phase < MAX_PHASES) {
            phase++;
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime); // AI logika z EnemyCharacter
        // prepnutie fazy podla HP
        if (phase == 1 && hp < maxHp * 0.66f) switchPhase();
        if (phase == 2 && hp < maxHp * 0.33f) switchPhase();

        if (specialCooldown > 0) specialCooldown -= deltaTime;
    }

    public int getPhase() { return phase; }
}
