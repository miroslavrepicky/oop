package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;

/**
 * Terminal state entered when the player's party is fully defeated.
 *
 * <p>Implements {@link TerminalState}: {@code GameScreen} calls {@link #execute}
 * instead of performing an {@code instanceof GameOverState} check.
 * Navigation is delegated to {@link AppController#retryLevel(int)}.
 */
public class GameOverState implements IGameState, TerminalState {

    /**
     * No-op – the game has ended; no further logic is executed in this state.
     */
    @Override public void update(float dt) { }

    /**
     * No-op – rendering is handled by {@code GameOverScreen} after the transition.
     */
    @Override public void render(float dt) { }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to {@link TerminalState} directly.
     */
    @Override public IGameState next() { return null; }


    @Override
    public void execute(AppController app, int currentLevel, int maxLevels) {
        app.goToGameOverScreen(currentLevel);
    }
}
