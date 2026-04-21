package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.GameManager;

/**
 * Terminal state entered when all enemies in the current level have been defeated.
 *
 * <p>Like {@link GameOverState}, both lifecycle methods are no-ops: {@code GameScreen}
 * detects this state type in {@code handleTransition()} and immediately switches to
 * {@code InventoryScreen} for the next level number.
 *
 * <p>The next level number is computed from the {@link GameManager} reference passed
 * at construction time rather than from a static field, ensuring the value stays
 * in sync with the actual game progress.
 */
public class LevelCompleteState implements IGameState {
    private final GameManager gameManager;

    /**
     * @param gm the shared {@link GameManager} instance used to compute the next level number
     */
    LevelCompleteState(GameManager gm) { this.gameManager = gm; }

    /**
     * No-op – the level has ended; no further game logic runs in this state.
     */
    @Override public void  update(float dt) { }

    /**
     * No-op – rendering is handled by {@code InventoryScreen} after the transition.
     */
    @Override public void  render(float dt) { }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to this state type directly.
     */
    @Override public IGameState next()      { return null; }

    /**
     * Returns the 1-based number of the next level to load.
     *
     * @return current level number + 1
     */
    public int nextLevelNumber() {
        return gameManager.getCurrentLevel().getLevelNumber() + 1;
    }
}
