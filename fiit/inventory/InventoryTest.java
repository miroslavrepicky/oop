package sk.stuba.fiit.inventory;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.characters.Knight;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    // ── Stub triedy bez LibGDX závislostí ─────────────────────────────────────

    /** Item stub – žiadny LibGDX, len sloty a ikona. */
    static class FakeItem extends Item {
        FakeItem(int slots) { super(slots, new Vector2D(0, 0)); }
        @Override public void use(PlayerCharacter c, Level l, Inventory inv) {}
        @Override public String getIconPath() { return "icon.png"; }
    }

    /**
     * Stub Knight-u – dedí od Knight (takže instanceof Knight == true),
     * ale nepotrebuje AnimationManager ani LibGDX atlas.
     * Všetky metódy volajúce OpenGL sú overridnuté na no-op / null.
     */
    public static class StubKnight extends PlayerCharacter {
        public StubKnight() {
            super("Knight", 100, 10, 1f, new Vector2D(0, 0), 0);
            this.enemy = false;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    /**
     * Stub ne-Knightovej postavy – nie je Knight, takže stojí 3 sloty.
     */
    static class StubPlayer extends PlayerCharacter {
        StubPlayer() {
            super("Wizzard", 100, 10, 1f, new Vector2D(0, 0), 0);
            this.enemy = false;
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public boolean isAlive() { return alive; }
        boolean alive = true;
    }

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(10);
    }

    // ── Základná inicializácia ────────────────────────────────────────────────

    @Test
    void defaultTotalSlots_is10() {
        assertEquals(10, inventory.getTotalSlots());
    }

    @Test
    void noArgConstructor_uses10Slots() {
        assertEquals(10, new Inventory().getTotalSlots());
    }

    @Test
    void initialUsedSlots_zero() {
        assertEquals(0, inventory.getUsedSlots());
    }

    // ── Pridanie a odobranie itemov ───────────────────────────────────────────

    @Test
    void addItem_succeedsWhenSlotsFit() {
        assertTrue(inventory.addItem(new FakeItem(2)));
        assertEquals(2, inventory.getUsedSlots());
        assertEquals(1, inventory.getItems().size());
    }

    @Test
    void addItem_failsWhenSlotsExceeded() {
        assertFalse(inventory.addItem(new FakeItem(11)));
        assertTrue(inventory.getItems().isEmpty());
    }

    @Test
    void addMultipleItems_accumulatesSlots() {
        inventory.addItem(new FakeItem(3));
        inventory.addItem(new FakeItem(4));
        assertEquals(7, inventory.getUsedSlots());
    }

    @Test
    void removeItem_freesSlots() {
        FakeItem item = new FakeItem(3);
        inventory.addItem(item);
        inventory.removeItem(item);
        assertEquals(0, inventory.getUsedSlots());
        assertTrue(inventory.getItems().isEmpty());
    }

    @Test
    void removeItem_notPresent_doesNotThrow() {
        assertDoesNotThrow(() -> inventory.removeItem(new FakeItem(1)));
    }

    @Test
    void getFreeSlots_isCorrect() {
        inventory.addItem(new FakeItem(4));
        assertEquals(6, inventory.getFreeSlots());
    }

    // ── Výber slotu ───────────────────────────────────────────────────────────

    @Test
    void selectNext_advancesSlot() {
        inventory.addItem(new FakeItem(1));
        inventory.addItem(new FakeItem(1));
        inventory.selectNext();
        assertEquals(1, inventory.getSelectedSlot());
    }

    @Test
    void selectNext_wrapsAround() {
        inventory.addItem(new FakeItem(1));
        inventory.addItem(new FakeItem(1));
        inventory.selectNext(); // → 1
        inventory.selectNext(); // → 0 (wrap)
        assertEquals(0, inventory.getSelectedSlot());
    }

    @Test
    void selectPrevious_wrapsAround() {
        inventory.addItem(new FakeItem(1));
        inventory.addItem(new FakeItem(1));
        inventory.selectPrevious(); // 0 → 1 (wrap)
        assertEquals(1, inventory.getSelectedSlot());
    }

    @Test
    void selectNext_emptyList_doesNotThrow() {
        assertDoesNotThrow(() -> inventory.selectNext());
    }

    @Test
    void selectPrevious_emptyList_doesNotThrow() {
        assertDoesNotThrow(() -> inventory.selectPrevious());
    }

    @Test
    void selectedSlot_adjustedAfterRemoveLastItem() {
        FakeItem a = new FakeItem(1);
        FakeItem b = new FakeItem(1);
        inventory.addItem(a);
        inventory.addItem(b);
        inventory.selectNext();          // slot = 1
        inventory.removeItem(b);         // list shrinks → slot clamped to 0
        assertEquals(0, inventory.getSelectedSlot());
    }

    // ── Správa postáv ─────────────────────────────────────────────────────────

    @Test
    void addCharacter_stubKnight_costs3Slots() {
        StubKnight knight = new StubKnight();
        inventory.addCharacter(knight);
        // StubKnight nie je instanceof Knight → stojí 3 sloty
        assertEquals(3, inventory.getUsedSlots());
    }

    @Test
    void addKnight_setsAsActive() {
        StubKnight knight = new StubKnight();
        inventory.addCharacter(knight);
        assertSame(knight, inventory.getActive());
    }

    @Test
    void addCharacter_nonKnight_costs3Slots() {
        StubPlayer player = new StubPlayer();
        inventory.addCharacter(player);
        assertEquals(3, inventory.getUsedSlots());
    }

    @Test
    void addCharacter_failsWhenNotEnoughSlots() {
        Inventory small = new Inventory(2);
        StubPlayer player = new StubPlayer(); // needs 3 slots
        assertFalse(small.addCharacter(player));
        assertTrue(small.getCharacters().isEmpty());
    }

    @Test
    void removeCharacter_stubKnight_canBeRemoved() {
        StubKnight knight = new StubKnight();
        inventory.addCharacter(knight);
        assertTrue(inventory.removeCharacter(knight)); // nie je base → dá sa odstrániť
    }

    @Test
    void removeCharacter_nonBase_freesSlots() {
        StubPlayer player = new StubPlayer();
        inventory.addCharacter(player);
        assertEquals(3, inventory.getUsedSlots());

        assertTrue(inventory.removeCharacter(player));
        assertEquals(0, inventory.getUsedSlots());
    }

    @Test
    void removeCharacter_notInList_returnsFalse() {
        StubPlayer stranger = new StubPlayer();
        assertFalse(inventory.removeCharacter(stranger));
    }

    @Test
    void isBaseCharacter_falseForStubKnight() {
        StubKnight knight = new StubKnight();
        inventory.addCharacter(knight);
        assertFalse(inventory.isBaseCharacter(knight)); // správne – nie je base
    }

    @Test
    void isBaseCharacter_falseForNonBase() {
        StubPlayer player = new StubPlayer();
        inventory.addCharacter(player);
        assertFalse(inventory.isBaseCharacter(player));
    }

    @Test
    void getActive_returnsFirstAdded() {
        StubKnight knight = new StubKnight();
        inventory.addCharacter(knight);
        assertSame(knight, inventory.getActive());
    }

    // ── isPartyDefeated ───────────────────────────────────────────────────────

    @Test
    void isPartyDefeated_trueWhenAllDead() {
        StubPlayer player = new StubPlayer();
        player.alive = false;
        inventory.addCharacter(player);
        assertTrue(inventory.isPartyDefeated());
    }

    @Test
    void isPartyDefeated_falseWhenSomeAlive() {
        StubPlayer player = new StubPlayer();
        player.alive = true;
        inventory.addCharacter(player);
        assertFalse(inventory.isPartyDefeated());
    }

    @Test
    void isPartyDefeated_emptyParty_returnsTrue() {
        // noneMatch on empty stream → true
        assertTrue(inventory.isPartyDefeated());
    }

    // ── switchCharacter ───────────────────────────────────────────────────────

    @Test
    void switchCharacter_validIndex_changesActive() {
        StubPlayer p1 = new StubPlayer();
        StubPlayer p2 = new StubPlayer();
        inventory.addCharacter(p1);
        inventory.addCharacter(p2);
        inventory.switchCharacter(2);
        assertSame(p2, inventory.getActive());
    }

    @Test
    void switchCharacter_invalidIndex_doesNotThrow() {
        inventory.addCharacter(new StubPlayer());
        assertDoesNotThrow(() -> inventory.switchCharacter(99));
    }

    @Test
    void switchCharacter_sameCharacter_noChange() {
        StubPlayer p = new StubPlayer();
        inventory.addCharacter(p);
        inventory.switchCharacter(1); // already active
        assertSame(p, inventory.getActive());
    }
}
