package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {
    private GameManager gameManager;
    private CollisionManager collisionManager;
    private PlayerController playerController;
    private GameRenderer gameRenderer;
    private final ShadowQuest game;

    public GameScreen(ShadowQuest game) {
        this.game        = game;
        gameManager      = GameManager.getInstance();
        collisionManager = new CollisionManager();
        playerController = new PlayerController(collisionManager); // zdielany CollisionManager
        gameRenderer     = new GameRenderer();
        gameRenderer.setCollisionManager(collisionManager);
    }

    @Override
    public void render(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            gameRenderer.toggleDebugHitboxes();
        }
        GameState state = gameManager.getGameState();

        if (state == GameState.GAME_OVER) {
            game.setScreen(new GameOverScreen(game, gameManager.getCurrentLevel().getLevelNumber()));
            return;
        }
        if (state == GameState.LEVEL_COMPLETE) {
            int nextLevel = gameManager.getCurrentLevel().getLevelNumber() + 1;
            game.setScreen(new InventoryScreen(game, nextLevel));
            return;
        }
        if (state == GameState.WIN) {
            game.setScreen(new WinScreen(game));
            return;
        }
        if (state == GameState.PLAYING) {
            playerController.update(deltaTime);
        }
        gameManager.update(deltaTime);
        collisionManager.update(gameManager.getCurrentLevel());
        gameRenderer.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        gameRenderer.resize(width, height);
    }

    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}

    @Override
    public void dispose() {
        gameRenderer.dispose();
    }
}
