package sk.stuba.fiit.core;

/**
 * Stav: krátkodobý prechod po smrti party – level ešte beží (animácia
 * smrti), ale hráč už nestráda. Po uplynutí {@link #DELAY} sekúnd
 * prejde na GameOverState.
 *
 * Nahradza:
 *   GameManager.gameOverTimer + if (state == GAME_OVER_DELAY) currentLevel.update(...)
 */
public class GameOverDelayState implements IGameState {

    /** Čas (sekundy) kým sa objaví obrazovka prehry. */
    public static final float DELAY = 3.0f;

    private final GameManager  gameManager;
    private final GameRenderer gameRenderer;
    private float              timer;
    private IGameState         nextState = null;

    public GameOverDelayState(GameManager gameManager,
                              GameRenderer gameRenderer,
                              float delay) {
        this.gameManager  = gameManager;
        this.gameRenderer = gameRenderer;
        this.timer        = delay;
    }

    @Override
    public void update(float deltaTime) {
        // Level stále beží – prebiehajú animácie smrti nepriateľov/hrdinov
        if (gameManager.getCurrentLevel() != null) {
            gameManager.getCurrentLevel().update(deltaTime);
        }

        timer -= deltaTime;
        if (timer <= 0f) {
            nextState = new GameOverState();
        }
    }

    @Override
    public void render(float deltaTime) {
        // Rovnaký renderer ako pri hraní – žiaden overlay zatiaľ
        gameRenderer.render(deltaTime);
    }

    @Override
    public IGameState next() {
        return nextState;
    }
}
