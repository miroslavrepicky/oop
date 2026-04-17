package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import sk.stuba.fiit.core.*;
import sk.stuba.fiit.core.states.*;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.render.GameRenderer;

/**
 * GameScreen po refaktore na State vzor + RenderSnapshot.
 *
 * Zmeny oproti pôvodnému kódu:
 *  - Odstránené {@code gameRenderer.setCollisionManager()} –
 *    GameRenderer už CollisionManager nepotrebuje (HUDRenderer
 *    dostáva boolean nearbyItemAvailable cez snapshot).
 *  - Všetko ostatné zostáva rovnaké.
 */
public class GameScreen implements Screen {

    private final ShadowQuest       game;
    private       IGameState        currentState;

    private final GameManager       gameManager;
    private final CollisionManager  collisionManager;
    private final PlayerController  playerController;
    private final GameRenderer      gameRenderer;

    public GameScreen(ShadowQuest game) {
        this.game = game;

        gameManager      = GameManager.getInstance();
        collisionManager = new CollisionManager();
        gameRenderer     = new GameRenderer();
        // Poznámka: setCollisionManager() odstránené – GameRenderer ho nepotrebuje
        playerController = new PlayerController(collisionManager);

        currentState = new PlayingState(
            playerController, gameManager, collisionManager, gameRenderer);
    }

    @Override
    public void render(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            gameRenderer.toggleDebugHitboxes();
        }

        currentState.update(deltaTime);

        IGameState next = currentState.next();
        if (next != null) {
            handleTransition(next);
            return;
        }

        currentState.render(deltaTime);
    }

    private void handleTransition(IGameState next) {
        if (next instanceof GameOverState) {
            int level = gameManager.getCurrentLevel().getLevelNumber();
            game.setScreen(new GameOverScreen(game, level));

        } else if (next instanceof LevelCompleteState) {
            int nextLevel = ((LevelCompleteState) next).nextLevelNumber();
            game.setScreen(new InventoryScreen(game, nextLevel));

        } else if (next instanceof WinState) {
            game.setScreen(new WinScreen(game));

        } else {
            currentState = next;
        }
    }

    @Override public void resize(int w, int h) { gameRenderer.resize(w, h); }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        gameRenderer.dispose();
    }
}
