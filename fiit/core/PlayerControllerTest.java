package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre PlayerController.
 *
 * Gdx.input je mockovaný – všetky isKeyPressed / isKeyJustPressed vrátia false.
 * Testy pokrývajú:
 * – konštrukciu (bez NPE)
 * – update() keď nie je žiaden hráč (early return)
 * – update() keď existuje hráč (žiaden kláves nestlačený → len gravitácia)
 * – update() keď existuje level (žiadna kolízia)
 */
class PlayerControllerTest extends GdxTest {

    @BeforeAll
    static void mockInput() {
        Gdx.input = mock(Input.class);
        // Všetky key-check metódy vrátia false (default Mockito správanie pre boolean)
    }

    @BeforeEach
    void resetGameManager() {
        GameManager.getInstance().resetGame();
    }

    // ── Konštrukcia ───────────────────────────────────────────────────────────

    @Test
    void construction_doesNotThrow() {
        assertDoesNotThrow(() -> new PlayerController(new CollisionManager()));
    }

    @Test
    void construction_withNullCollisionManager_doesNotThrow() {
        // CollisionManager je len uložený – NPE nastane až pri pickupNearbyItem()
        assertDoesNotThrow(() -> new PlayerController(null));
    }

    // ── update() – bez hráča ─────────────────────────────────────────────────

    @Test
    void update_withNoActivePlayer_doesNotThrow() {
        // GameManager po resete nemá hráča
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

    // ── update() – s hráčom ───────────────────────────────────────────────────

    @Test
    void update_withActivePlayer_noKeyPressed_doesNotThrow() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayerController pc = new PlayerController(new CollisionManager());
            // Žiaden kláves nestlačený → update aplikuje len gravitáciu
            assertDoesNotThrow(() -> pc.update(0.016f));
        });
    }

    @Test
    void update_withActivePlayer_playerRemainsAlive() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayerController pc = new PlayerController(new CollisionManager());
            pc.update(0.016f);
            // Samotný update bez kláves nesmie zabiť hráča
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

    // ── Pomocné metódy ────────────────────────────────────────────────────────

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
