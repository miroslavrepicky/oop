package sk.stuba.fiit.characters;

import org.junit.jupiter.api.Test;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;

class PlayerCharacterLogicTest {

    static class StubPC extends PlayerCharacter {
        StubPC(String name, int hp, int atk, float speed, int maxArmor) {
            super(name, hp, atk, speed, new Vector2D(0,0), maxArmor);
            this.enemy = false;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    @Test void getName()              { assertEquals("Hero", new StubPC("Hero",100,20,2f,50).getName()); }
    @Test void getHp()                { assertEquals(100, new StubPC("H",100,20,2f,50).getHp()); }
    @Test void getMaxHp()             { assertEquals(100, new StubPC("H",100,20,2f,50).getMaxHp()); }
    @Test void getAttackPower()       { assertEquals(20, new StubPC("H",100,20,2f,50).getAttackPower()); }
    @Test void getSpeed()             { assertEquals(2f, new StubPC("H",100,10,2f,50).getSpeed(), 0.001f); }
    @Test void isEnemy_false()        { assertFalse(new StubPC("H",100,10,2f,50).isEnemy()); }
    @Test void isAlive_initially()    { assertTrue(new StubPC("H",100,10,2f,50).isAlive()); }
    @Test void isAlive_afterDeath()   { StubPC p = new StubPC("H",100,10,2f,0); p.takeDamage(200); assertFalse(p.isAlive()); }

    @Test void getCurrentMana_minusOne()  { assertEquals(-1, new StubPC("H",100,10,2f,50).getCurrentMana()); }
    @Test void getMaxMana_minusOne()      { assertEquals(-1, new StubPC("H",100,10,2f,50).getMaxMana()); }
    @Test void getArrowCount_minusOne()   { assertEquals(-1, new StubPC("H",100,10,2f,50).getArrowCount()); }
    @Test void getMaxArrows_minusOne()    { assertEquals(-1, new StubPC("H",100,10,2f,50).getMaxArrows()); }

    @Test void primaryAttack_null_doesNotThrow()   { assertDoesNotThrow(() -> new StubPC("H",100,10,2f,50).performPrimaryAttack(null)); }
    @Test void secondaryAttack_null_doesNotThrow() { assertDoesNotThrow(() -> new StubPC("H",100,10,2f,50).performSecondaryAttack(null)); }

    @Test void move_updatesPosition() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.move(new Vector2D(10f,5f));
        assertEquals(10f, p.getPosition().getX(), 0.001f);
        assertEquals(5f,  p.getPosition().getY(), 0.001f);
    }

    @Test void move_updatesHitbox() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.move(new Vector2D(15f,0f));
        assertEquals(15f, p.getHitbox().x, 0.001f);
    }

    @Test void revive_restoresHp() {
        StubPC p = new StubPC("H",100,10,2f,0);
        p.takeDamage(200); p.revive();
        assertEquals(100, p.getHp()); assertTrue(p.isAlive());
    }

    @Test void jump_onGround_setsVelocity() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.setOnGround(true); p.jump(300f);
        assertEquals(300f, p.getVelocityY(), 0.001f);
    }

    @Test void jump_airborne_ignored() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.setOnGround(false); p.setVelocityY(100f); p.jump(300f);
        assertEquals(100f, p.getVelocityY(), 0.001f);
    }

    @Test void setFacingRight_false() { StubPC p = new StubPC("H",100,10,2f,50); p.setFacingRight(false); assertFalse(p.isFacingRight()); }

    @Test void addArmor_increases() {
        StubPC p = new StubPC("H",100,10,2f,100);
        p.addArmor(30); assertEquals(30, p.getArmor());
    }

    @Test void addArmor_cappedAtMax() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.addArmor(200); assertEquals(50, p.getArmor());
    }

    @Test void setHitboxSize() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.setHitboxSize(new Vector2D(48f,80f));
        assertEquals(48f, p.getHitbox().width, 0.001f);
    }

    @Test void updateHitbox_syncs() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.getPosition().setX(77f); p.getPosition().setY(33f); p.updateHitbox();
        assertEquals(77f, p.getHitbox().x, 0.001f);
    }

    @Test void applySlow_reducesSpeed() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.applySlow(0.5f,5f); assertEquals(1f, p.getSpeed(), 0.001f);
    }

    @Test void applyDot_ticksDamage() {
        StubPC p = new StubPC("H",100,10,2f,0);
        p.applyDot(10,2f); p.tickEffects(1f);
        assertTrue(p.getHp() < 100);
    }

    @Test void updateAnimation_nullAm_doesNotThrow() {
        assertDoesNotThrow(() -> new StubPC("H",100,10,2f,50).updateAnimation(new UpdateContext(0.016f)));
    }

    @Test void onCollision_doesNotThrow() {
        assertDoesNotThrow(() -> new StubPC("H",100,10,2f,50).onCollision("anything"));
    }

    @Test void restoreStats_setsValues() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.takeDamage(999); p.restoreStats(75, 20);
        assertEquals(75, p.getHp()); assertEquals(20, p.getArmor());
    }

    @Test void restoreStats_clampedToMax() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.restoreStats(999,999);
        assertEquals(100, p.getHp()); assertEquals(50, p.getArmor());
    }

    @Test void velocityX_setAndGet() {
        StubPC p = new StubPC("H",100,10,2f,50);
        p.setVelocityX(42f); assertEquals(42f, p.getVelocityX(), 0.001f);
    }
}
