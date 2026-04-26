package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre PlayerController.
 *
 * Gdx.input je mockovany – vsetky isKeyPressed / isKeyJustPressed vratia false.
 * Testy pokryvaju:
 * – konstrukciu (bez NPE)
 * – update() ked nie je ziaden hrac (early return)
 * – update() ked existuje hrac (ziaden klaves nestlaceny -> len gravitacia)
 * – update() ked existuje level (ziadna kolizia)
 */
class PlayerControllerTest extends GdxTest {

    @BeforeAll
    static void mockInput() {
        Gdx.input = mock(Input.class);
        // Vsetky key-check metody vratia false (default Mockito spravanie pre boolean)
    }

    @BeforeEach
    void resetGameManager() {
        GameManager.getInstance().resetGame();
    }

    //  Konstrukcia

    @Test
    void construction_doesNotThrow() {
        assertDoesNotThrow(() -> new PlayerController(new CollisionManager()));
    }

    @Test
    void construction_withNullCollisionManager_doesNotThrow() {
        // CollisionManager je len ulozeny – NPE nastane az pri pickupNearbyItem()
        assertDoesNotThrow(() -> new PlayerController(null));
    }

    //  update() – bez hraca

    @Test
    void update_withNoActivePlayer_doesNotThrow() {
        // GameManager po resete nema hraca
        PlayerController pc = new PlayerController(new CollisionManager());
        assertDoesNotThrow(() -> pc.update(0.016f));
    }

    @Test
    void update_withNoActivePlayer_multipleFrames_doesNotThrow() {
        PlayerController pc = new PlayerController(new CollisionManager());
        for (int i = 0; i < 30; i++) {
            assertDoesNotThrow(() -> pc.update(0.016f));
        }
    }

    //  update() – s hracom

    @Test
    void update_withActivePlayer_noKeyPressed_doesNotThrow() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayerController pc = new PlayerController(new CollisionManager());
            // ziaden kláves nestlaceny -> update aplikuje len gravitáciu
            assertDoesNotThrow(() -> pc.update(0.016f));
        });
    }

    @Test
    void update_withActivePlayer_playerRemainsAlive() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayerController pc = new PlayerController(new CollisionManager());
            pc.update(0.016f);
            // Samotny update bez kláves nesmie zabit hráca
            assertTrue(GameManager.getInstance().getInventory().getActive().isAlive());
        });
    }

    @Test
    void update_withActivePlayer_multipleFrames_doesNotThrow() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayerController pc = new PlayerController(new CollisionManager());
            for (int i = 0; i < 60; i++) {
                assertDoesNotThrow(() -> pc.update(0.016f));
            }
        });
    }

    //  Pomocne metody

    private void withAnimMock(Runnable block) {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, (mock, ctx) -> {
                     when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
                     when(mock.hasAnimation(anyString())).thenReturn(false);
                     when(mock.getAnimationDuration(anyString())).thenReturn(0.5f);
                     when(mock.getFrameCount(anyString())).thenReturn(5);
                 })) {
            block.run();
        }
    }
}
