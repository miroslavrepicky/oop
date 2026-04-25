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
 * Doplnkové testy pre PlayerCharacter – pokrýva časti nevypočítané v PlayerCharacterLogicTest:
 * – executeAttack s kontrolou many
 * – attack lifecycle (isAttacking flag, timer)
 * – template metódy getMana / spendMana pre subtriedu
 * – updateAnimation keď AnimationManager == null
 */
class PlayerCharacterAttackTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    /** Hráč s neobmedzenou manou (default implementácia). */
    static class UnlimitedManaPC extends PlayerCharacter {
        UnlimitedManaPC() { super("Hero", 100, 20, 2f, new Vector2D(0, 0), 0); enemy = false; }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    /** Hráč s reálnou manou – testuje getMana / spendMana template. */
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

    /** Útok s nulovou manou – vracia null (melee-like). */
    static class FreeAttack implements Attack {
        int executeCount = 0;
        @Override public Projectile execute(Character attacker, Level level) { executeCount++; return null; }
        @Override public String getAnimationName() { return "attack"; }
        @Override public float  getAnimationDuration(AnimationManager am) { return 0.4f; }
        @Override public int    getManaCost() { return 0; }
    }

    /** Útok s vyššou manou – testuje blokovanie. */
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

    // ── executeAttack – blokovanie ─────────────────────────────────────────────

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
        assertFalse(pc.isAttacking, "Bez levelu nesmie útok začať");
    }

    @Test
    void executeAttack_insufficientMana_blocked() {
        ManaPC pc = new ManaPC(10); // mana = 10
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel()); // cost = 50
        assertFalse(pc.isAttacking, "Útok sa nesmie spustiť bez dostatku many");
    }

    @Test
    void executeAttack_insufficientMana_manaNotConsumed() {
        ManaPC pc = new ManaPC(10);
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel());
        assertEquals(10, pc.mana, "Mana sa nesmie spotrebovať pri blokovanom útoku");
    }

    @Test
    void executeAttack_sufficientMana_consumed() {
        ManaPC pc = new ManaPC(100);
        pc.executeAttack(new ExpensiveAttack(), new FakeLevel()); // cost = 50
        // AnimationManager je null → executeAttack na null AM preskočí bez isAttacking=true
        // Ale keď AM je null, kód skontroluje: if (am != null) → false → isAttacking zostane false
        // Takže mana sa spotrebuje iba keď AM nie je null? Overme to
        // Podľa kódu: spendMana() sa volá PRED null-check AM:
        //   spendMana(cost);
        //   if (level == null) return;
        //   AnimationManager am = getAnimationManager();
        //   if (am != null) { isAttacking = true; ... }
        // Takže mana sa spotrebuje aj keď AM je null
        assertEquals(50, pc.mana, "Mana sa má spotrebovať pred AM kontrolou");
    }

    // ── executeAttack – úspešný štart ─────────────────────────────────────────

    /** Pomocný PC s mocknutým AnimationManager-om (bez Gdx). */
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
        StubAnimManager() { super(null); } // nulový atlas – žiaden Gdx.files
        @Override public void play(String name) { playedAnim = name; }
        @Override public void update(float dt) {}
        @Override public boolean hasAnimation(String name) { return true; }
        @Override public float getAnimationDuration(String name) { return 0.6f; }
        @Override public int   getFrameCount(String name) { return 6; }
        @Override public TextureAtlas.AtlasRegion getCurrentFrame() { return null; }
    }

    // AnimationManager.TextureAtlasFrame nie je public API – použijeme inak.
    // Namiesto toho overíme len isAttacking flag cez performPrimaryAttack.

    @Test
    void executeAttack_whileAttacking_ignored() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.isAttacking = true; // simulate ongoing attack
        FreeAttack atk = new FreeAttack();
        pc.executeAttack(atk, new FakeLevel());
        // Nesmie resetovať timer ani znovu zavolať execute
        assertEquals(0, atk.executeCount);
    }

    // ── Template metódy mana ──────────────────────────────────────────────────

    @Test
    void getMana_defaultImplementation_returnsMaxValue() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertEquals(Integer.MAX_VALUE, pc.getMana());
    }

    @Test
    void spendMana_defaultImplementation_noOp() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.spendMana(999); // nesmie vyhodiť ani zmeniť stav
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

    // ── Attack state flag ─────────────────────────────────────────────────────

    @Test
    void isAttacking_initiallyFalse() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertFalse(pc.isAttacking);
    }

    @Test
    void performPrimaryAttack_nullPrimary_doesNotSetAttacking() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        // primaryAttack je null keď sa nenastaví (v UnlimitedManaPC)
        pc.performPrimaryAttack(new FakeLevel());
        assertFalse(pc.isAttacking);
    }

    @Test
    void performSecondaryAttack_nullSecondary_doesNotSetAttacking() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        pc.performSecondaryAttack(new FakeLevel());
        assertFalse(pc.isAttacking);
    }

    // ── updateAnimation – null AnimationManager ───────────────────────────────

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

    // ── hasAnimation ─────────────────────────────────────────────────────────

    @Test
    void hasAnimation_nullAm_returnsFalse() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertFalse(pc.hasAnimation("jump"));
    }

    // ── onCollision ───────────────────────────────────────────────────────────

    @Test
    void onCollision_doesNotThrow() {
        UnlimitedManaPC pc = new UnlimitedManaPC();
        assertDoesNotThrow(() -> pc.onCollision("anything"));
        assertDoesNotThrow(() -> pc.onCollision(null));
    }
}
