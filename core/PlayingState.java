package sk.stuba.fiit.core;

/**
 * Stav: hra beží – hráč sa pohybuje, AI útočí, kolízie sa riešia.
 *
 * Zapuzdruje logiku, ktorá bola predtým rozhádzaná v:
 *   GameScreen.render()  → if (state == PLAYING) playerController.update(...)
 *   GameManager.update() → if (state == PLAYING) currentLevel.update(...)
 */
public class PlayingState implements IGameState {

    private final PlayerController playerController;
    private final GameManager      gameManager;
    private final CollisionManager collisionManager;
    private final GameRenderer     gameRenderer;

    /** Nasledujúci stav nastavený zvnútra (pri splnení podmienky prechodu). */
    private IGameState nextState = null;

    public PlayingState(PlayerController playerController,
                        GameManager      gameManager,
                        CollisionManager collisionManager,
                        GameRenderer     gameRenderer) {
        this.playerController = playerController;
        this.gameManager      = gameManager;
        this.collisionManager = collisionManager;
        this.gameRenderer     = gameRenderer;
    }

    @Override
    public void update(float deltaTime) {
        playerController.update(deltaTime);
        gameManager.update(deltaTime);
        collisionManager.update(gameManager.getCurrentLevel());

        // --- prechody stavu ---
        GameState gs = gameManager.getGameState();

        if (gs == GameState.GAME_OVER_DELAY) {
            nextState = new GameOverDelayState(
                gameManager, gameRenderer, GameOverDelayState.DELAY);
            return;
        }
        if (gs == GameState.LEVEL_COMPLETE) {
            nextState = new LevelCompleteState(gameManager);
            return;
        }
        if (gs == GameState.WIN) {
            nextState = new WinState();
            return;
        }
    }

    @Override
    public void render(float deltaTime) {
        // Celé vykresľovanie levelu – žiaden špeciálny overlay
        gameRenderer.render(deltaTime);
    }

    @Override
    public IGameState next() {
        IGameState result = nextState;
        nextState = null;  // reset – next() sa nesmie volať viac raz za frame
        return result;
    }
}
