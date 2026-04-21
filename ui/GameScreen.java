package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import sk.stuba.fiit.core.*;
import sk.stuba.fiit.core.states.*;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.render.GameRenderer;

/**
 * LibGDX {@link Screen} that hosts the active gameplay loop.
 *
 * <p>Delegates all game logic to the State pattern: a {@link IGameState} instance
 * receives {@code update(dt)} and {@code render(dt)} calls every frame.
 * When a state signals a transition via {@link IGameState#next()}, this screen
 * calls {@link #handleTransition(IGameState)} to switch either the internal
 * state or the LibGDX screen.
 *
 * <h2>State transitions handled here</h2>
 * <ul>
 *   <li>{@link GameOverState}  → {@link GameOverScreen} for the failed level number.</li>
 *   <li>{@link LevelCompleteState} → {@link InventoryScreen} for the next level number.</li>
 *   <li>{@link WinState}       → {@link WinScreen}.</li>
 *   <li>Any other state → replaced as the new {@code currentState} (e.g. pause/resume).</li>
 * </ul>
 *
 * <h2>Debug shortcut</h2>
 * <p>Pressing F1 toggles hitbox outlines via {@link GameRenderer#toggleDebugHitboxes()}.
 */
public class GameScreen implements Screen {

    private final ShadowQuest       game;

    /** The currently active game-state machine node. */
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

    /**
     * Responds to a state transition request produced by the current state.
     *
     * <p>Terminal states ({@link GameOverState}, {@link LevelCompleteState}, {@link WinState})
     * cause a LibGDX screen change. All other states replace {@link #currentState} in-place
     * (e.g. transitioning between {@link PlayingState} and {@link sk.stuba.fiit.core.states.PausedState}).
     *
     * @param next the next state returned by {@link IGameState#next()}
     */
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
