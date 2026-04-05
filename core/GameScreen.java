package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {
    private GameManager gameManager;
    private CollisionManager collisionManager;
    private PlayerController playerController;
    private GameRenderer gameRenderer;

    public GameScreen() {
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
        playerController.update(deltaTime);
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
