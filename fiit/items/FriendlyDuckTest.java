package sk.stuba.fiit.items;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
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

class FriendlyDuckTest extends GdxTest {

    private MockedConstruction.MockInitializer<AnimationManager> animStub() {
        return (mock, ctx) -> {
            when(mock.getAnimationSize(any())).thenReturn(new Vector2D(46, 33));
            when(mock.getFirstFrameSize(any())).thenReturn(new Vector2D(46, 33));
        };
    }

    static class StubPlayer extends PlayerCharacter {
        public StubPlayer(boolean facingRight) {
            super("TestPlayer", 100, 10, 1f, new Vector2D(100, 100), 0);
            setFacingRight(facingRight);
        }
        @Override public AnimationManager getAnimationManager() { return null; }
        @Override public void update(UpdateContext ctx) {}
        @Override public void move(Vector2D d) { position = position.add(d); updateHitbox(); }
    }

    @Test
    void friendlyDuck_initializationAndProperties() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            Vector2D pos = new Vector2D(10, 20);
            FriendlyDuck duck = new FriendlyDuck(pos);

            assertEquals(1, duck.getSlotsRequired());
            assertEquals("icons/duck.png", duck.getIconPath());
            assertEquals(pos.getX(), duck.getPosition().getX());
        }
    }

    @Test
    void use_addsProjectileAndRemovesFromInventory() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));
            StubPlayer player = new StubPlayer(true);
            Level levelMock = mock(Level.class);
            Inventory inventoryMock = mock(Inventory.class);

            duck.use(player, levelMock, inventoryMock);

            verify(levelMock, times(1)).addProjectile(any(TurdflyProjectile.class));
            verify(inventoryMock, times(1)).removeItem(duck);
        }
    }

    @Test
    void use_nullLevel_doesNothing() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));
            StubPlayer player = new StubPlayer(true);
            Inventory inventoryMock = mock(Inventory.class);

            duck.use(player, null, inventoryMock);

            verify(inventoryMock, never()).removeItem(any());
        }
    }

    @Test
    void update_updatesAnimationManager() {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, animStub())) {
            FriendlyDuck duck = new FriendlyDuck(new Vector2D(0, 0));
            UpdateContext ctx = new UpdateContext(0.1f);
            assertDoesNotThrow(() -> duck.update(ctx));
        }
    }
}
