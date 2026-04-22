package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;

/**
 * Marker + callback interface for states that end the current game screen.
 *
 * <p>Motivation: {@code GameScreen.handleTransition()} originally contained
 * three {@code instanceof} checks ({@code GameOverState}, {@code LevelCompleteState},
 * {@code WinState}). Every new terminal state required a change to {@code GameScreen}.
 *
 * <p>After refactoring: each terminal state implements this interface and
 * encapsulates its own navigation decision. {@code GameScreen} performs one
 * single {@code instanceof TerminalState} check – nothing more.
 *
 * <p>Implemented by: {@link GameOverState}, {@link LevelCompleteState}, {@link WinState}.
 */
public interface TerminalState {

    /**
     * Called by {@code GameScreen} when this state is returned from
     * {@link IGameState#next()}. The implementation calls the appropriate
     * {@link AppController} method to trigger navigation.
     *
     * @param app          application controller used for navigation
     * @param currentLevel 1-based number of the level that just ended
     * @param maxLevels    total number of levels in the game
     */
    void execute(AppController app, int currentLevel, int maxLevels);
}
