package sk.stuba.fiit.core;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;

public class GameScreen implements Screen {
    private GameManager gameManager;
    private PlayerController playerController;
    private GameRenderer gameRenderer;
    private CollisionManager collisionManager;

    public GameScreen() {
        gameManager = GameManager.getInstance();
        playerController = new PlayerController();
        gameRenderer = new GameRenderer();
        collisionManager = new CollisionManager();
    }

    @Override
    public void render(float deltaTime) {
        playerController.update(deltaTime);
        gameManager.update(deltaTime);
        collisionManager.update(gameManager.getCurrentLevel());
        gameRenderer.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        gameRenderer.resize(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        gameRenderer.dispose();
    }
}
