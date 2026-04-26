package sk.stuba.fiit.characters;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.attacks.Attack;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Doplnkove testy pre PlayerCharacter – pokryva casti nevypocitane v PlayerCharacterLogicTest:
 * – executeAttack s kontrolou many
 * – attack lifecycle (isAttacking flag, timer)
 * – template metody getMana / spendMana pre subtriedu
 * – updateAnimation ked AnimationManager == null
 */
class PlayerCharacterAttackTest {

    //  Stubs

    /** Hrac s neobmedzenou manou (default implementacia). */
    static class UnlimitedManaPC extends PlayerCharacter {
        UnlimitedManaPC() { super("Hero", 100, 20, 2f, new Vector2D(0, 0), 0); enemy = false; }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    /** Hrac s realnou manou – testuje getMana / spendMana template. */
    static class ManaPC extends PlayerCharacter {
        int mana;
        ManaPC(int startMana) {
            super("Mage", 100, 10, 2f, new Vector2D(0, 0), 0);
            enemy = false;
            mana = startMana;
        }
        @Override protected int  getMana()             { return mana; }
        @Override protected void spendMana(int amount) { mana = Math.max(0, mana - amount); }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    /** utok s nulovou manou – vracia null (melee-like). */
    static class FreeAttack implements Attack {
        int executeCount = 0;
        @Override public Projectile execute(Character attacker, Level level) { executeCount++; return null; }
        @Override public String getAnimationName() { return "attack"; }
        @Override public float  getAnimationDuration(AnimationManager am) { return 0.4f; }
        @Override public int    getManaCost() { return 0; }
    }

    /** utok s vyssou manou – testuje blokovanie. */
    static class ExpensiveAttack implements Attack {
        @Override public Projectile execute(Character attacker, Level level) { return null; }
        @Override public String getAnimationName() { return "attack"; }
        @Override public float  getAnimationDuration(AnimationManager am) { return 0.4f; }
        @Override public int    getManaCost() { return 50; }
    }

    /** Level stub bez atlasov. */
    static class FakeLevel extends Level {
        FakeLevel() { super(1); }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    //  executeAttack – blokovanie

    @Test
    void executeAttack_nullAttack_doesNotStart() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.executeAttack(null, new FakeLevel());
        assertFalse(pc.isAttacking);
    }

    @Test
    void executeAttack_nullLevel_doesNotStart() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        FreeAttack atk = new FreeAttack();
        pc.executeAttack(atk, null);
        assertFalse(pc.isAttacking, "Bez levelu nesmie utok zacat");
    }

    @Test
    void executeAttack_insufficientMana_blocked() {
        ManaPC pc = new ManaPC(10); // mana = 10
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel()); // cost = 50
        assertFalse(pc.isAttacking, "utok sa nesmie spustit bez dostatku many");
    }

    @Test
    void executeAttack_insufficientMana_manaNotConsumed() {
        ManaPC pc = new ManaPC(10);
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel());
        assertEquals(10, pc.mana, "Mana sa nesmie spotrebovat pri blokovanom utoku");
    }

    @Test
    void executeAttack_sufficientMana_consumed() {
        ManaPC pc = new ManaPC(100);
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel()); // cost = 50
        // AnimationManager je null -> executeAttack na null AM preskoci bez isAttacking=true
        // Ale ked AM je null, kod skontroluje: if (am != null) -> false -> isAttacking zostane false
        // Takze mana sa spotrebuje iba ked AM nie je null? Overme to
        // Podla kodu: spendMana() sa volá PRED null-check AM:
        //   spendMana(cost);
        //   if (level == null) return;
        //   AnimationManager am = getAnimationManager();
        //   if (am != null) { isAttacking = true; ... }
        // Takze mana sa spotrebuje aj ked AM je null
        assertEquals(50, pc.mana, "Mana sa má spotrebovat pred AM kontrolou");
    }

    //  executeAttack – uspesny start

    /** Pomocny PC s mocknutym AnimationManager-om (bez Gdx). */
    static class AnimatedPC extends PlayerCharacter {
        final StubAnimManager stubAm;
        AnimatedPC() {
            super("Animated", 100, 10, 2f, new Vector2D(0, 0), 0);
            enemy = false;
            stubAm = new StubAnimManager();
        }
        @Override public AnimationManager getAnimationManager() { return stubAm; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    /** Minimálny stub AnimationManager bez atlasu. */
    static class StubAnimManager extends AnimationManager {
        String playedAnim = null;
        StubAnimManager() { super(null); } // nulovy atlas – ziaden Gdx.files
        @Override public void play(String name) { playedAnim = name; }
        @Override public void update(float dt) {}
        @Override public boolean hasAnimation(String name) { return true; }
        @Override public float getAnimationDuration(String name) { return 0.6f; }
        @Override public int   getFrameCount(String name) { return 6; }
        @Override public TextureAtlas.AtlasRegion getCurrentFrame() { return null; }
    }

    // AnimationManager.TextureAtlasFrame nie je public API – pouzijeme inak.
    // Namiesto toho overime len isAttacking flag cez performPrimaryAttack.

    @Test
    void executeAttack_whileAttacking_ignored() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.isAttacking = true; // simulate ongoing attack
        FreeAttack atk = new FreeAttack();
        pc.executeAttack(atk, new FakeLevel());
        // Nesmie resetovat timer ani znovu zavolat execute
        assertEquals(0, atk.executeCount);
    }

    //  Template metody mana

    @Test
    void getMana_defaultImplementation_returnsMaxValue() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertEquals(Integer.MAX_VALUE, pc.getMana());
    }

    @Test
    void spendMana_defaultImplementation_noOp() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.spendMana(999); // nesmie vyhodit ani zmenit stav
        assertEquals(Integer.MAX_VALUE, pc.getMana());
    }

    @Test
    void getMana_customImplementation_returnsField() {
        ManaPC pc = new ManaPC(75);
        assertEquals(75, pc.getMana());
    }

    @Test
    void spendMana_customImplementation_reducesField() {
        ManaPC pc = new ManaPC(75);
        pc.spendMana(25);
        assertEquals(50, pc.getMana());
    }

    @Test
    void spendMana_customImplementation_clampedToZero() {
        ManaPC pc = new ManaPC(10);
        pc.spendMana(9999);
        assertEquals(0, pc.getMana());
    }

    //  Attack state flag

    @Test
    void isAttacking_initiallyFalse() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertFalse(pc.isAttacking);
    }

    @Test
    void performPrimaryAttack_nullPrimary_doesNotSetAttacking() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        // primaryAttack je null ked sa nenastavi (v UnlimitedManaPC)
        pc.performPrimaryAttack(new FakeLevel());
        assertFalse(pc.isAttacking);
    }

    @Test
    void performSecondaryAttack_nullSecondary_doesNotSetAttacking() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.performSecondaryAttack(new FakeLevel());
        assertFalse(pc.isAttacking);
    }

    //  updateAnimation – null AnimationManager

    @Test
    void updateAnimation_nullAm_doesNotThrow() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        UpdateContext ctx = new UpdateContext(0.016f);
        assertDoesNotThrow(() -> pc.updateAnimation(ctx));
    }

    @Test
    void updateAnimation_whileAttacking_nullAm_doesNotThrow() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.isAttacking = true;
        pc.attackAnimTimer = 0.3f;
        UpdateContext ctx = new UpdateContext(0.016f);
        assertDoesNotThrow(() -> pc.updateAnimation(ctx));
    }

    //  hasAnimation

    @Test
    void hasAnimation_nullAm_returnsFalse() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertFalse(pc.hasAnimation("jump"));
    }

    //  onCollision

    @Test
    void onCollision_doesNotThrow() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertDoesNotThrow(() -> pc.onCollision("anything"));
        assertDoesNotThrow(() -> pc.onCollision(null));
    }
}
