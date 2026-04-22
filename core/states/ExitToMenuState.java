package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;

/**
 * Terminal state triggered from {@link PausedState} when the player presses ESC.
 * {@code GameScreen} detects it as a {@link TerminalState} and calls
 * {@link AppController#goToMainMenu()}.
 */
final class ExitToMenuState implements IGameState, TerminalState {
    @Override public void update(float dt) {}
    @Override public void render(float dt) {}
    @Override public IGameState next() { return null; }

    @Override
    public void execute(AppController app, int currentLevel, int maxLevels) {
        app.goToMainMenu();
    }
}
