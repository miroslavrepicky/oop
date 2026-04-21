package sk.stuba.fiit.core.states;

import sk.stuba.fiit.core.AppController;

/**
 * Terminálny stav spustený z PausedState keď hráč stlačí ESC.
 * GameScreen ho zachytí ako TerminalState a zavolá goToMainMenu().
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
