package sk.stuba.fiit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.exceptions.GameStateException;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre GameManager singleton.
 * Každý test začína resetom singletonu, aby boli testy na sebe nezávislé.
 * AtlasCache.dispose() je bezpečné keď cache je prázdna (žiaden dispose GL volania).
 */
class GameManagerTest extends GdxTest {

    @BeforeEach
    void resetSingleton() {
        // resetGame() čistí inventár, level, atlachy (prázdny cache = žiadny GL volanie)
        // a ProjectilePool (iba ArrayDeque.clear)
        GameManager.getInstance().resetGame();
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    @Test
    void getInstance_notNull() {
        assertNotNull(GameManager.getInstance());
    }

    @Test
    void getInstance_returnsSameInstance() {
        assertSame(GameManager.getInstance(), GameManager.getInstance());
    }

    // ── resetGame ─────────────────────────────────────────────────────────────

    @Test
    void resetGame_inventoryIsEmpty() {
        GameManager.getInstance().resetGame();
        assertTrue(GameManager.getInstance().getInventory().getCharacters().isEmpty());
    }

    @Test
    void resetGame_currentLevelIsNull() {
        GameManager.getInstance().resetGame();
        assertNull(GameManager.getInstance().getCurrentLevel());
    }

    @Test
    void resetGame_inventoryNotNull() {
        GameManager.getInstance().resetGame();
        assertNotNull(GameManager.getInstance().getInventory());
    }

    @Test
    void resetGame_calledTwice_doesNotThrow() {
        assertDoesNotThrow(() -> {
            GameManager.getInstance().resetGame();
            GameManager.getInstance().resetGame();
        });
    }

    // ── initGame ─────────────────────────────────────────────────────────────

    @Test
    void initGame_addsKnightToParty() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            assertEquals(1, GameManager.getInstance().getInventory().getCharacters().size());
        });
    }

    @Test
    void initGame_activeCharacterIsKnight() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            assertEquals("Knight", GameManager.getInstance().getInventory().getActive().getName());
        });
    }

    @Test
    void initGame_activeCharacterNotNull() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            assertNotNull(GameManager.getInstance().getInventory().getActive());
        });
    }

    // ── startLevel – validácia vstupu ─────────────────────────────────────────

    @Test
    void startLevel_throwsGameStateException_whenNoActivePlayer() {
        // Po resete nie je žiaden hráč
        assertThrows(GameStateException.class,
            () -> GameManager.getInstance().startLevel(1));
    }

    @Test
    void startLevel_throwsGameStateException_forLevelZero() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            assertThrows(GameStateException.class,
                () -> GameManager.getInstance().startLevel(0));
        });
    }

    @Test
    void startLevel_throwsGameStateException_forNegativeLevel() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            assertThrows(GameStateException.class,
                () -> GameManager.getInstance().startLevel(-1));
        });
    }

    @Test
    void startLevel_throwsGameStateException_whenLevelTooHigh() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            int max = GameManager.getInstance().getMaxLevels();
            assertThrows(GameStateException.class,
                () -> GameManager.getInstance().startLevel(max + 1));
        });
    }

    // ── getMaxLevels ─────────────────────────────────────────────────────────

    @Test
    void getMaxLevels_atLeastOne() {
        assertTrue(GameManager.getInstance().getMaxLevels() >= 1);
    }

    // ── reviveParty ───────────────────────────────────────────────────────────

    @Test
    void reviveParty_restoresKnightHp() {
        withAnimMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();

            var active = gm.getInventory().getActive();
            active.takeDamage(9999);
            assertFalse(active.isAlive(), "Hráč mal byť mŕtvy pred oživením");

            gm.reviveParty();
            assertTrue(active.isAlive(), "Hráč má byť živý po reviveParty()");
        });
    }

    @Test
    void reviveParty_restoresFullHp() {
        withAnimMock(() -> {
            GameManager gm = GameManager.getInstance();
            gm.initGame();
            var active = gm.getInventory().getActive();
            active.takeDamage(9999);

            gm.reviveParty();
            assertEquals(active.getMaxHp(), active.getHp());
        });
    }

    @Test
    void reviveParty_withEmptyParty_doesNotThrow() {
        // prázdna strana – forEach iba nič nerobí
        assertDoesNotThrow(() -> GameManager.getInstance().reviveParty());
    }

    // ── getInventory ─────────────────────────────────────────────────────────

    @Test
    void getInventory_notNull() {
        assertNotNull(GameManager.getInstance().getInventory());
    }

    @Test
    void getInventory_afterReset_hasNoItems() {
        assertTrue(GameManager.getInstance().getInventory().getItems().isEmpty());
    }

    // ── getCurrentLevel ───────────────────────────────────────────────────────

    @Test
    void getCurrentLevel_nullBeforeStartLevel() {
        assertNull(GameManager.getInstance().getCurrentLevel());
    }

    // ── Pomocné metódy ────────────────────────────────────────────────────────

    /**
     * Vykonáva blok kódu s mocknutým AnimationManager-om
     * (interceptuje new AnimationManager(path) → stub).
     */
    private void withAnimMock(Runnable block) {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class,
                     (mock, ctx) -> when(mock.getFirstFrameSize("idle"))
                         .thenReturn(new Vector2D(32, 64)))) {
            block.run();
        }
    }
}
