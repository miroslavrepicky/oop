package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.PlayerController;
import sk.stuba.fiit.core.states.GameOverState;
import sk.stuba.fiit.core.states.IGameState;
import sk.stuba.fiit.core.states.LevelCompleteState;
import sk.stuba.fiit.core.states.PlayingState;
import sk.stuba.fiit.core.states.WinState;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.render.GameRenderer;

/**
 * LibGDX {@link Screen} that hosts the active gameplay loop.
 *
 * <h2>MVC placement</h2>
 * <p>This class sits at the boundary between the View and the Controller layer.
 * It owns the game-state machine ({@link IGameState}) and delegates:
 * <ul>
 *   <li>all game logic to concrete {@link IGameState} implementations;</li>
 *   <li>all rendering to {@link GameRenderer} via snapshots built by
 *       {@link sk.stuba.fiit.render.SnapshotBuilder};</li>
 *   <li>all navigation and business-logic transitions to
 *       {@link AppController}.</li>
 * </ul>
 *
 * <p>The screen never calls {@link GameManager} for mutations; it holds a
 * reference only to read the current level number when a game-over or
 * level-complete state is detected.
 *
 * <h2>State machine transitions handled here</h2>
 * <ul>
 *   <li>{@link GameOverState}      → {@link AppController#retryLevel(int)}
 *       which shows {@link GameOverScreen}.</li>
 *   <li>{@link LevelCompleteState} → {@link AppController#startGame(int)}
 *       for the next level <em>via</em> {@link InventoryScreen}. (Navigation
 *       handled inside {@code AppController} implementation.)</li>
 *   <li>{@link WinState}           → {@link AppController#goToMainMenu()}
 *       after first showing {@link WinScreen}.</li>
 *   <li>Any other state → replaced as the new {@link #currentState}
 *       (e.g. pause / resume transitions).</li>
 * </ul>
 *
 * <h2>Debug shortcut</h2>
 * <p>Pressing {@code F1} toggles hitbox outlines via
 * {@link GameRenderer#toggleDebugHitboxes()}.
 */
public class GameScreen implements Screen {

    private final AppController     app;
    private       IGameState        currentState;
    private final GameManager       gameManager;
    private final CollisionManager  collisionManager;
    private final PlayerController  playerController;
    private final GameRenderer      gameRenderer;

    /**
     * Constructs the screen, initialises all subsystems, and enters the
     * {@link PlayingState}.
     *
     * @param app the application controller used for screen transitions;
     *            must not be {@code null}
     */
    public GameScreen(AppController app) {
        this.app = app;

        gameManager      = GameManager.getInstance();
        collisionManager = new CollisionManager();
        gameRenderer     = new GameRenderer();
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

    // ── Transition handling ───────────────────────────────────────────────────

    /**
     * Responds to a state-transition request produced by the current state.
     *
     * <p>Terminal states ({@link GameOverState}, {@link LevelCompleteState},
     * {@link WinState}) cause a screen change via {@link AppController}.
     * All other states replace {@link #currentState} in-place (e.g. the
     * pause/resume cycle).
     *
     * <p>Note that {@link LevelCompleteState} navigates to
     * {@link InventoryScreen} rather than directly starting the next level,
     * giving the player a chance to adjust their loadout.
     *
     * @param next the next state returned by {@link IGameState#next()}
     */
    private void handleTransition(IGameState next) {
        if (next instanceof GameOverState) {
            // Read-only query – no mutation occurs here.
            int level = gameManager.getCurrentLevel().getLevelNumber();
            app.retryLevel(level);

        } else if (next instanceof LevelCompleteState) {
            int nextLevel = ((LevelCompleteState) next).nextLevelNumber();
            if (nextLevel > gameManager.getMaxLevels()) {
                app.goToWinScreen();
            } else {
                // Send the player to inventory so they can prepare for the next level.
                // AppController.startGame() would skip the inventory screen,
                // so we use the dedicated goToInventory() method instead.
                app.goToInventory(nextLevel);
            }

        } else if (next instanceof WinState) {
            app.goToWinScreen();

        } else {
            currentState = next;
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override public void resize(int w, int h) { gameRenderer.resize(w, h); }
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}

    @Override
    public void dispose() {
        gameRenderer.dispose();
    }
}
