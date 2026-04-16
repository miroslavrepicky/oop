package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Stav: hra beží – hráč sa pohybuje, AI útočí, kolízie sa riešia.
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
        // --- 1. Kontrola vstupu pre Pauzu ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            nextState = new PausedState(gameRenderer, this);
            return;
        }

        // --- 2. Update hernej logiky ---
        playerController.update(deltaTime);

        if (gameManager.getCurrentLevel() != null) {
            gameManager.getCurrentLevel().update(deltaTime);
        }
        collisionManager.update(gameManager.getCurrentLevel());

        // --- 3. Prechody stavu (Vyhodnotenie podmienok) ---
        if (gameManager.getInventory().isPartyDefeated()) {
            nextState = new GameOverDelayState(
                gameManager, gameRenderer, GameOverDelayState.DELAY);
            return;
        }

        if (gameManager.getCurrentLevel() != null && gameManager.getCurrentLevel().isCompleted()) {
            int nextLevel = gameManager.getCurrentLevel().getLevelNumber() + 1;
            if (nextLevel > gameManager.getMaxLevels()) {
                nextState = new WinState();
            } else {
                nextState = new LevelCompleteState(gameManager);
            }
        }
    }

    @Override
    public void render(float deltaTime) {
        gameRenderer.render(deltaTime);
    }

    @Override
    public IGameState next() {
        IGameState result = nextState;
        nextState = null;  // reset
        return result;
    }
}
