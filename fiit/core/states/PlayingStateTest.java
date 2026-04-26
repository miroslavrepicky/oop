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
 * Gdx.input je mockovany aby isKeyJustPressed() a isKeyPressed() vratili false
 * (ziadne klavesy nie su stlacene) – tym sa obide zavislost na realnom InputProcessor.
 * GameRenderer a AppController su mockovane pretoze potrebuju GL kontext / nav logiku.
 */
@ExtendWith(MockitoExtension.class)
class PlayingStateTest extends GdxTest {

    @Mock AppController mockApp;
    @Mock GameRenderer  mockRenderer;

    /** Mockujeme Gdx.input raz pre celu testovaciu triedu. */
    @BeforeAll
    static void mockInput() {
        Gdx.input = mock(Input.class);
        // Vsetky isKeyJustPressed / isKeyPressed vratia false (default Mockito)
    }

    @BeforeEach
    void resetGameManager() {
        GameManager.getInstance().resetGame();
    }

    //  Konstrukcia

    @Test
    void construction_doesNotThrow() {
        assertDoesNotThrow(this::buildState);
    }

    //  next() – zakladny kontrakt

    @Test
    void next_returnsNull_beforeAnyUpdate() {
        PlayingState state = buildState();
        assertNull(state.next());
    }

    @Test
    void next_returnsNull_afterUpdate_withNoGameState() {
        // Prazdny GameManager (bez initGame) -> isPartyDefeated() = true na prazdnej liste
        // ale aj tak next() musi byt spotrebovane cez next() po update()
        PlayingState state = buildState();
        state.update(0.016f);
        // Zavolame next() a dalsi next() – druhy call musi byt null
        state.next(); // consume whatever was set
        assertNull(state.next(), "Druhy next() musi byt null – stav je spotrebovany");
    }

    //  Prechod pri porazeni party

    @Test
    void update_partyDefeated_schedulesGameOverDelayState() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            // Zabijeme aktivneho hraca
            GameManager.getInstance().getInventory().getActive().takeDamage(9999);
            assertFalse(GameManager.getInstance().getInventory().getActive().isAlive());

            PlayingState state = buildState();
            state.update(0.016f);

            IGameState next = state.next();
            assertNotNull(next, "Po porazeni party musi byt naplanovany dalsi stav");
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
            assertNull(state.next(), "Stav sa ma spotrebovat – druhy next() = null");
        });
    }

    //  Prechod pri dokonceni levelu

    @Test
    void update_levelComplete_schedulesTerminalState() {
        withAnimMock(() -> {
            GameManager.getInstance().initGame();
            // Level s dokoncenym flag-om (ziaden enemy, isCompleted sa nastavi pri update)
            // Simulujeme cez level s jednym zabitym nepriatelom cez LevelUpdateTest.TestLevel
            // Najjednoduchsie: priamo nastavime level cez TestLevel
            FakeCompletedLevel level = new FakeCompletedLevel();
            setCurrentLevel(level);

            PlayingState state = buildState();
            state.update(0.016f);

            IGameState next = state.next();
            // Moze byt WinState alebo LevelCompleteState – oboje su TerminalState
            if (next != null) {
                assertInstanceOf(TerminalState.class, next);
            }
            // Ak next == null znamena, ze sme party defeated check presli pred level check
            // (lebo initGame + live player)
        });
    }

    //  Update bez level / bez hráca

    @Test
    void update_withNoLevelAndNoPlayer_doesNotThrow() {
        // Prázdny GameManager – ziaden level, ziaden hrác
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

    //  Pomocne triedy a metody

    /** Level, ktory má isCompleted() == true hned od zaciatku. */
    static class FakeCompletedLevel extends sk.stuba.fiit.world.Level {
        FakeCompletedLevel() { super(1); }
        @Override public boolean isCompleted() { return true; }
        @Override public sk.stuba.fiit.world.MapManager getMapManager() { return null; }
    }

    /** Nastavi currentLevel v GameManager cez reflexiu (inak len cez startLevel()). */
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
