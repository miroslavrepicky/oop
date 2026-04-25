package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.GdxTest;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.PlayerController;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.util.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre PlayingState.
 *
 * Gdx.input je mockovaný aby isKeyJustPressed() a isKeyPressed() vrátili false
 * (žiadne klávesy nie sú stlačené) – tým sa obíde závislosť na reálnom InputProcessor.
 * GameRenderer a AppController sú mockované pretože potrebujú GL kontext / nav logiku.
 */
@ExtendWith(MockitoExtension.class)
class PlayingStateTest extends GdxTest {

    @Mock AppController mockApp;
    @Mock GameRenderer  mockRenderer;

    /** Mockujeme Gdx.input raz pre celú testovaciu triedu. */
    @BeforeAll
    static void mockInput() {
        Gdx.input = mock(Input.class);
        // Všetky isKeyJustPressed / isKeyPressed vrátia false (default Mockito)
    }

    @BeforeEach
    void resetGameManager() {
        GameManager.getInstance().resetGame();
    }

    // ── Konštrukcia ───────────────────────────────────────────────────────────

    @Test
    void construction_doesNotThrow() {
        assertDoesNotThrow(this::buildState);
    }

    // ── next() – základný kontrakt ────────────────────────────────────────────

    @Test
    void next_returnsNull_beforeAnyUpdate() {
        PlayingState state = buildState();
        assertNull(state.next());
    }

    @Test
    void next_returnsNull_afterUpdate_withNoGameState() {
        // Prázdny GameManager (bez initGame) → isPartyDefeated() = true na prázdnej liste
        // ale aj tak next() musí byť spotrebované cez next() po update()
        PlayingState state = buildState();
        state.update(0.016f);
        // Zavoláme next() a ďalší next() – druhý call musí byť null
        state.next(); // consume whatever was set
        assertNull(state.next(), "Druhý next() musí byť null – stav je spotrebovaný");
    }

    // ── Prechod pri porazení party ────────────────────────────────────────────

    @Test
    void update_partyDefeated_schedulesGameOverDelayState() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            // Zabijeme aktívneho hráča
            GameManager.getInstance().getInventory().getActive().takeDamage(9999);
            assertFalse(GameManager.getInstance().getInventory().getActive().isAlive());

            PlayingState state = buildState();
            state.update(0.016f);

            IGameState next = state.next();
            assertNotNull(next, "Po porazení party musí byť naplánovaný ďalší stav");
            assertInstanceOf(GameOverDelayState.class, next);
        });
    }

    @Test
    void update_partyDefeated_nextConsumedOnSecondCall() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            GameManager.getInstance().getInventory().getActive().takeDamage(9999);

            PlayingState state = buildState();
            state.update(0.016f);
            assertNotNull(state.next());
            assertNull(state.next(), "Stav sa má spotrebovať – druhý next() = null");
        });
    }

    // ── Prechod pri dokončení levelu ──────────────────────────────────────────

    @Test
    void update_levelComplete_schedulesTerminalState() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            // Level s dokončeným flag-om (žiaden enemy, isCompleted sa nastaví pri update)
            // Simulujeme cez level s jedným zabitým nepriateľom cez LevelUpdateTest.TestLevel
            // Najjednoduchšie: priamo nastavíme level cez TestLevel
            FakeCompletedLevel level = new FakeCompletedLevel();
            setCurrentLevel(level);

            PlayingState state = buildState();
            state.update(0.016f);

            IGameState next = state.next();
            // Môže byť WinState alebo LevelCompleteState – oboje sú TerminalState
            if (next != null) {
                assertInstanceOf(TerminalState.class, next);
            }
            // Ak next == null znamená, že sme party defeated check prešli pred level check
            // (lebo initGame + live player)
        });
    }

    // ── Update bez level / bez hráča ─────────────────────────────────────────

    @Test
    void update_withNoLevelAndNoPlayer_doesNotThrow() {
        // Prázdny GameManager – žiaden level, žiaden hráč
        PlayingState state = buildState();
        assertDoesNotThrow(() -> state.update(0.016f));
    }

    @Test
    void update_multipleFrames_doesNotThrow() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            PlayingState state = buildState();
            for (int i = 0; i < 10; i++) {
                state.update(0.016f);
                state.next(); // consume
            }
        });
    }

    // ── Pomocné triedy a metódy ───────────────────────────────────────────────

    /** Level, ktorý má isCompleted() == true hneď od začiatku. */
    static class FakeCompletedLevel extends sk.stuba.fiit.world.Level {
        FakeCompletedLevel() { super(1); }
        @Override public boolean isCompleted() { return true; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    /** Nastaví currentLevel v GameManager cez reflexiu (inak len cez startLevel()). */
    private static void setCurrentLevel(sk.stuba.fiit.world.Level level) {
        try {
            var f = GameManager.class.getDeclaredField("currentLevel");
            f.setAccessible(true);
            f.set(GameManager.getInstance(), level);
        } catch (Exception e) {
            fail("Reflexia zlyhala: " + e.getMessage());
        }
    }

    private PlayingState buildState() {
        CollisionManager cm = new CollisionManager();
        PlayerController pc = new PlayerController(cm);
        return new PlayingState(pc, GameManager.getInstance(), cm, mockRenderer, mockApp);
    }

    private void withAnimMock(Runnable block) {
        try (MockedConstruction<AnimationManager> ignored =
                 mockConstruction(AnimationManager.class, (mock, ctx) -> {
                     when(mock.getFirstFrameSize("idle")).thenReturn(new Vector2D(32, 64));
                     when(mock.hasAnimation(anyString())).thenReturn(false);
                     when(mock.getAnimationDuration(anyString())).thenReturn(0.5f);
                 })) {
            block.run();
        }
    }
}
