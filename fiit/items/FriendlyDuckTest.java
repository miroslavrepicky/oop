package sk.stuba.fiit.items;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sk.stuba.fiit.HeadlessGdxTest;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.projectiles.TurdflyProjectile;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FriendlyDuckTest extends HeadlessGdxTest {

    // Pomocny stub pre hraca, aby sme nemuseli riesit animacie hraca
    static class StubPlayer extends PlayerCharacter {
        public StubPlayer(boolean facingRight) {
            super("TestPlayer", 100, 10, 1f, new Vector2D(100, 100), 0);
            setFacingRight(facingRight);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
    }

    @Test
    void friendlyDuck_initializationAndProperties() {
        Vector2D pos = new Vector2D(10, 20);
        FriendlyDuck duck = new FriendlyDuck(pos);

        // Overenie zakladnych vlastnosti zdedenych z Item
        assertEquals(1, duck.getSlotsRequired(), "FriendlyDuck by mala zaberat 1 slot");
        assertEquals("icons/duck.png", duck.getIconPath());
        assertEquals(pos.getX(), duck.getPosition().getX());
    }

    @Test
    void use_addsProjectileAndRemovesFromInventory() {
        // Priprava
        FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));
        StubPlayer player = new StubPlayer(true); // Hrac sa pozera doprava
        Level levelMock = mock(Level.class);
        Inventory inventoryMock = mock(Inventory.class);

        // Vykonanie
        duck.use(player, levelMock, inventoryMock);

        // Overenie:
        // 1. Projektil bol pridany do levelu
        verify(levelMock, times(1)).addProjectile(any(TurdflyProjectile.class));

        // 2. Polozka bola odstranena z inventara po pouziti
        verify(inventoryMock, times(1)).removeItem(duck);
    }

    @Test
    void use_nullLevel_doesNothing() {
        FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));
        StubPlayer player = new StubPlayer(true);
        Inventory inventoryMock = mock(Inventory.class);

        // Ak je level null, metoda by mala skoncit bez vedlajsich ucinkov
        duck.use(player, null, inventoryMock);

        verify(inventoryMock, never()).removeItem(any());
    }

    @Test
    void update_updatesAnimationManager() {
        FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));

        // Pouzijeme konstruktor, ktory vyzaduje len deltaTime
        UpdateContext ctx = new UpdateContext(0.1f);

        // Overime, ze update metoda prebehne bez vyhodenia NullPointerException
        // FriendlyDuck interne vola animationManager.update(ctx.deltaTime)
        assertDoesNotThrow(() -> duck.update(ctx));
    }
}
