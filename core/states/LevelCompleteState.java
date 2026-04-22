package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;

/**
 * Terminal state entered when all enemies in the current level have been defeated.
 *
 * <p>Implements {@link TerminalState}: the navigation decision (next level vs. win
 * screen) that previously lived in {@code GameScreen.handleTransition()} is now
 * encapsulated here. {@code GameScreen} performs a single
 * {@code instanceof TerminalState} check and calls {@link #execute}.
 */
public class LevelCompleteState implements IGameState, TerminalState {

    private final GameManager gameManager;

    /**
     * @param gm the shared {@link GameManager} instance used to compute the next level number
     */
    LevelCompleteState(GameManager gm) { this.gameManager = gm; }

    /**
     * No-op – the level has ended; no further game logic runs in this state.
     */
    @Override public void update(float dt) { }

    /**
     * No-op – rendering is handled by {@code InventoryScreen} after the transition.
     */
    @Override public void render(float dt) { }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to {@link TerminalState} directly.
     */
    @Override public IGameState next() { return null; }

    /**
     * Returns the 1-based number of the next level to load.
     *
     * @return current level number + 1
     */
    public int nextLevelNumber() {
        return gameManager.getCurrentLevel().getLevelNumber() + 1;
    }

    /**
     * Navigates to the inventory screen for the next level, or to the win screen
     * if all levels have been completed.
     */
    @Override
    public void execute(AppController app, int currentLevel, int maxLevels) {
        int nextLevel = nextLevelNumber();
        if (nextLevel > maxLevels) {
            app.goToWinScreen();
        } else {
            app.goToInventory(nextLevel);
        }
    }
}
