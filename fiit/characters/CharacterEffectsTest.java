package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;
import static org.junit.jupiter.api.Assertions.*;

class CharacterEffectsTest {

    static class TC extends Character {
        TC() { super("T", 100, 10, 5f, new Vector2D(0,0), 0, 0); }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public void onCollision(Object o) {}
    }

    @Test
    void revive_clearsDot() {
        TC c = new TC();
        c.applyDot(50, 10f);
        c.revive();
        // After revive, tick should not deal damage (dot cleared)
        int hp = c.getHp();
        c.tickEffects(1f);
        assertEquals(hp, c.getHp());
    }

    @Test
    void revive_clearsSlow() {
        TC c = new TC();
        float originalSpeed = c.getSpeed();
        c.applySlow(0.1f, 100f);
        c.revive();
        assertEquals(originalSpeed, c.getSpeed(), 0.001f);
    }

    @Test
    void applySlow_replacesPreviousSlow() {
        TC c = new TC();
        float base = c.getSpeed();
        c.applySlow(0.5f, 10f);  // speed = 2.5
        c.applySlow(0.4f, 10f);  // should restore to base first, then apply 0.4
        assertEquals(base * 0.4f, c.getSpeed(), 0.001f);
    }

    @Test
    void tickEffects_dotAccumulates_overMultipleFrames() {
        TC c = new TC();
        c.applyDot(10, 5f); // 10 dps
        c.tickEffects(0.05f); // 0.5 dmg — not enough for int
        c.tickEffects(0.05f); // 1.0 total — should deal 1 damage
        assertTrue(c.getHp() < 100);
    }

    static class TCWithArmor extends Character {
        TCWithArmor() { super("T", 100, 10, 5f, new Vector2D(0,0), 0, 50); } // maxArmor=50
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public void onCollision(Object o) {}
    }

    @Test
    void addArmor_increasesUpToMax() {
        TCWithArmor c = new TCWithArmor(); // maxArmor=50, armor=0
        c.addArmor(30);
        assertEquals(30, c.getArmor());
    }

    @Test
    void addArmor_cappedAtMaxArmor() {
        TCWithArmor c = new TCWithArmor();
        c.addArmor(100); // over max
        assertEquals(50, c.getArmor()); // capped at maxArmor
    }

    @Test
    void getMaxArmor_returnsConstructorValue() {
        TC c = new TC();
        assertEquals(0, c.getMaxArmor());
    }

    @Test
    void isEnemy_defaultTrue() {
        TC c = new TC();
        assertTrue(c.isEnemy());
    }
}
