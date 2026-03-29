package sk.stuba.fiit.characters;

import sk.stuba.fiit.attacks.MeleeAttack;
import sk.stuba.fiit.attacks.SpellAttack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.util.Vector2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Wizzard extends PlayerCharacter {
    private static final int MAX_ARMOR = 30; // carodejnik ma nizku obranu

    private int mana;
    private int maxMana;
    private static final int SPELL_MANA_COST = 20;
    private AnimationManager animationManager;

    public Wizzard(Vector2D position) {
        super("Wizzard", 7000, 40, 2.5f, position, MAX_ARMOR);
        this.mana = 100;
        this.maxMana = 100;
        this.gravityStrategy = new NormalGravity();
        initAnimations();

        primaryAttack   = new SpellAttack(6.0f, 50f, 20);   // SPACE — rychle kuzlo
        secondaryAttack = new MeleeAttack(1);                // V — melee zaloha
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/wizzard/wizzard.atlas");
        animationManager.addAnimation("idle",   "IDLE/IDLE",     0.1f);
        animationManager.addAnimation("walk",   "WALK/WALK",     0.1f);
        animationManager.addAnimation("attack", "ATTACK/ATTACK", 0.07f);
        animationManager.addAnimation("cast",   "CAST/CAST",     0.08f);
        animationManager.addAnimation("death",  "DEATH/DEATH",   0.1f);
        animationManager.addAnimation("hurt",   "HURT/HURT",     0.08f);
    }

    // Wizzard override-ne mana metody z PlayerCharacter
    @Override
    protected int getMana() { return mana; }

    @Override
    protected void spendMana(int amount) {
        mana = Math.max(0, mana - amount);
    }

    @Override
    public void handleInput() {}

    @Override
    public void update(float deltaTime) {
        handleInput();
        regenerateMana(deltaTime);
    }

    private void regenerateMana(float deltaTime) {
        mana = Math.min(maxMana, mana + (int)(5 * deltaTime));
    }

    public int getMaxMana() { return maxMana; }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }
}
