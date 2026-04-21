package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;

/**
 * Terminal state entered when the player completes the final level.
 *
 * <p>Implements {@link TerminalState}: {@code GameScreen} calls {@link #execute}
 * instead of performing an {@code instanceof WinState} check.
 */
public class WinState implements IGameState, TerminalState {

    /**
     * No-op – the game has been won; no further logic runs in this state.
     */
    @Override public void update(float dt) { }

    /**
     * No-op – rendering is handled by {@code WinScreen} after the transition.
     */
    @Override public void render(float dt) { }

    /**
     * Always returns {@code null}; {@code GameScreen} reacts to {@link TerminalState} directly.
     */
    @Override public IGameState next() { return null; }

    /**
     * Navigates to the win screen.
     */
    @Override
    public void execute(AppController app, int currentLevel, int maxLevels) {
        app.goToWinScreen();
    }
}
