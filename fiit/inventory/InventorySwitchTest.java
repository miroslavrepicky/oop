package sk.stuba.fiit.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;
import static org.junit.jupiter.api.Assertions.*;

class InventorySwitchTest {

    static class StubChar extends PlayerCharacter {
        boolean alive = true;
        StubChar(String name) { super(name, 100, 10, 1f, new Vector2D(0,0), 0); enemy = false; }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
        @Override public boolean isAlive() { return alive; }
    }

    static class FakeItem extends Item {
        boolean used = false;
        FakeItem() { super(1, new Vector2D(0,0)); }
        @Override public void use(PlayerCharacter c, Level l, Inventory inv) { used = true; inv.removeItem(this); }
        @Override public String getIconPath() { return null; }
    }

    private Inventory inv;
    private StubChar c1, c2;

    @BeforeEach
    void setUp() {
        inv = new Inventory(10);
        c1 = new StubChar("Alpha");
        c2 = new StubChar("Beta");
        inv.addCharacter(c1);
        inv.addCharacter(c2);
    }

    @Test
    void switchToNextAlive_returnsTrue_whenAliveExists() {
        c1.alive = false;
        assertTrue(inv.switchToNextAlive());
        assertSame(c2, inv.getActive());
    }

    @Test
    void switchToNextAlive_returnsFalse_whenAllDead() {
        c1.alive = false;
        c2.alive = false;
        assertFalse(inv.switchToNextAlive());
    }

    @Test
    void switchToNextAlive_preservesPosition() {
        c1.getPosition().setX(99f);
        c1.getPosition().setY(42f);
        c1.alive = false;
        inv.switchToNextAlive();
        assertEquals(99f, c2.getPosition().getX(), 0.001f);
        assertEquals(42f, c2.getPosition().getY(), 0.001f);
    }

    @Test
    void useSelected_callsItemUse() {
        FakeItem item = new FakeItem();
        inv.addItem(item);
        inv.useSelected(c1, null);
        assertTrue(item.used);
    }

    @Test
    void useSelected_emptyItems_doesNotThrow() {
        assertDoesNotThrow(() -> inv.useSelected(c1, null));
    }

    @Test
    void useSelected_outOfRange_doesNotThrow() {
        // selectedSlot=0, items empty → guard fires
        assertDoesNotThrow(() -> inv.useSelected(c1, null));
    }

    @Test
    void switchCharacter_inheritsFacingDirection() {
        c1.setFacingRight(false);
        inv.switchCharacter(2);
        assertFalse(c2.isFacingRight());
    }

    @Test
    void removeActiveCharacter_changesActiveToPrevious() {
        inv.switchCharacter(2);
        assertSame(c2, inv.getActive());
        inv.removeCharacter(c2);
        assertSame(c1, inv.getActive());
    }
}
