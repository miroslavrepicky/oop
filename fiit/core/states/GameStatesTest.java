package sk.stuba.fiit.core.states;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.world.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for all terminal (and other simple) IGameState implementations.
 *
 * The test class is placed in package {@code sk.stuba.fiit.core.states} so that
 * package-private classes (ExitToMenuState) and package-private constructors
 * (LevelCompleteState) are accessible.
 */
@ExtendWith(MockitoExtension.class)
class GameStatesTest {

    @Mock AppController app;
    @Mock GameManager   gameManager;
    @Mock Level         mockLevel;

    //  IGameState contract
    // Helper: verifies all IGameState methods exist and don't throw for terminal states.

    //  GameOverState

    @Test
    void gameOverState_update_doesNotThrow() {
        assertDoesNotThrow(() -> new GameOverState().update(0.016f));
    }

    @Test
    void gameOverState_render_doesNotThrow() {
        assertDoesNotThrow(() -> new GameOverState().render(0.016f));
    }

    @Test
    void gameOverState_next_returnsNull() {
        assertNull(new GameOverState().next());
    }

    @Test
    void gameOverState_isTerminalState() {
        assertTrue(new GameOverState() instanceof TerminalState);
    }

    @Test
    void gameOverState_execute_callsGoToGameOverScreen() {
        new GameOverState().execute(app, 2, 5);
        verify(app).goToGameOverScreen(2);
    }

    @Test
    void gameOverState_execute_passesCurrentLevel() {
        new GameOverState().execute(app, 3, 5);
        verify(app).goToGameOverScreen(3);
    }

    //  WinState

    @Test
    void winState_update_doesNotThrow() {
        assertDoesNotThrow(() -> new WinState().update(0.016f));
    }

    @Test
    void winState_render_doesNotThrow() {
        assertDoesNotThrow(() -> new WinState().render(0.016f));
    }

    @Test
    void winState_next_returnsNull() {
        assertNull(new WinState().next());
    }

    @Test
    void winState_isTerminalState() {
        assertTrue(new WinState() instanceof TerminalState);
    }

    @Test
    void winState_execute_callsGoToWinScreen() {
        new WinState().execute(app, 1, 1);
        verify(app).goToWinScreen();
    }

    @Test
    void winState_execute_ignoresLevelArguments() {
        // goToWinScreen takes no arguments; current/max levels are irrelevant
        new WinState().execute(app, 99, 99);
        verify(app, times(1)).goToWinScreen();
        verifyNoMoreInteractions(app);
    }

    //  ExitToMenuState
    // Package-private class – accessible because this test is in the same package.

    @Test
    void exitToMenuState_update_doesNotThrow() {
        assertDoesNotThrow(() -> new ExitToMenuState().update(0.016f));
    }

    @Test
    void exitToMenuState_render_doesNotThrow() {
        assertDoesNotThrow(() -> new ExitToMenuState().render(0.016f));
    }

    @Test
    void exitToMenuState_next_returnsNull() {
        assertNull(new ExitToMenuState().next());
    }

    @Test
    void exitToMenuState_isTerminalState() {
        assertTrue(new ExitToMenuState() instanceof TerminalState);
    }

    @Test
    void exitToMenuState_execute_callsGoToMainMenu() {
        new ExitToMenuState().execute(app, 1, 5);
        verify(app).goToMainMenu();
    }

    //  LevelCompleteState
    // Package-private constructor – accessible because this test is in the same package.

    @Test
    void levelCompleteState_update_doesNotThrow() {

        assertDoesNotThrow(() -> new LevelCompleteState(gameManager).update(0.016f));
    }

    @Test
    void levelCompleteState_render_doesNotThrow() {

        assertDoesNotThrow(() -> new LevelCompleteState(gameManager).render(0.016f));
    }

    @Test
    void levelCompleteState_next_returnsNull() {

        assertNull(new LevelCompleteState(gameManager).next());
    }

    @Test
    void levelCompleteState_isTerminalState() {

        assertTrue(new LevelCompleteState(gameManager) instanceof TerminalState);
    }

    @Test
    void levelCompleteState_nextLevelNumber_isCurrentPlusOne() {
        when(gameManager.getCurrentLevel()).thenReturn(mockLevel);
        when(mockLevel.getLevelNumber()).thenReturn(2);

        LevelCompleteState state = new LevelCompleteState(gameManager);
        assertEquals(3, state.nextLevelNumber());
    }

    @Test
    void levelCompleteState_execute_goesToInventory_whenMoreLevelsRemain() {
        when(gameManager.getCurrentLevel()).thenReturn(mockLevel);
        when(mockLevel.getLevelNumber()).thenReturn(1);

        new LevelCompleteState(gameManager).execute(app, 1, 5);
        verify(app).goToInventory(2); // nextLevel = 2, maxLevels = 5 -> not last
    }

    @Test
    void levelCompleteState_execute_goesToWinScreen_whenLastLevel() {
        when(gameManager.getCurrentLevel()).thenReturn(mockLevel);
        when(mockLevel.getLevelNumber()).thenReturn(1);

        // nextLevel = 2 > maxLevels = 1 -> win
        new LevelCompleteState(gameManager).execute(app, 1, 1);
        verify(app).goToWinScreen();
    }

    @Test
    void levelCompleteState_execute_exactBoundary_goesToInventory() {
        when(gameManager.getCurrentLevel()).thenReturn(mockLevel);
        when(mockLevel.getLevelNumber()).thenReturn(2);

        // nextLevel = 3, maxLevels = 3 -> nextLevel == maxLevels, so goToInventory(3)
        // 3 > 3 is false -> goToInventory(3)
        new LevelCompleteState(gameManager).execute(app, 2, 3);
        verify(app).goToInventory(3);
    }

    //  GameOverDelayState – IGameState contract
    // Full rendering test is skipped (needs GameRenderer/Level), but the state
    // machine logic (timer countdown -> next()) can be tested with mocks.

    @Test
    void gameOverDelayState_next_returnsNullWhileTimerActive() {
        // Use mocks for GameManager and null renderer (not called in this code path)
        GameOverDelayState state = new GameOverDelayState(gameManager, null, 1.0f);
        when(gameManager.getInventory()).thenReturn(new sk.stuba.fiit.inventory.Inventory());
        when(gameManager.getCurrentLevel()).thenReturn(null);

        state.update(0.5f); // half-way through
        assertNull(state.next(), "Should remain in delay state while timer > 0");
    }

    @Test
    void gameOverDelayState_next_returnsGameOverState_afterTimerExpires() {
        GameOverDelayState state = new GameOverDelayState(gameManager, null, 0.1f);
        when(gameManager.getInventory()).thenReturn(new sk.stuba.fiit.inventory.Inventory());
        when(gameManager.getCurrentLevel()).thenReturn(null);

        state.update(0.2f); // exceeds delay
        IGameState next = state.next();
        assertNotNull(next);
        assertTrue(next instanceof GameOverState);
    }

    @Test
    void gameOverDelayState_isNotTerminalState() {
        // GameOverDelayState itself is transitional, not terminal
        assertFalse(new GameOverDelayState(gameManager, null, 1.0f) instanceof TerminalState);
    }
}
