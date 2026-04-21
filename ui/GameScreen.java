package sk.stuba.fiit.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.PlayerController;
import sk.stuba.fiit.core.states.IGameState;
import sk.stuba.fiit.core.states.PlayingState;
import sk.stuba.fiit.core.states.TerminalState;
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
 *   <li>all navigation and business-logic transitions to {@link AppController}.</li>
 * </ul>
 *
 * <h2>State machine transitions</h2>
 * <p>Terminal states ({@link sk.stuba.fiit.core.states.GameOverState},
 * {@link sk.stuba.fiit.core.states.LevelCompleteState},
 * {@link sk.stuba.fiit.core.states.WinState}) implement {@link TerminalState}
 * and self-describe their navigation action. {@code GameScreen} performs a single
 * {@code instanceof TerminalState} check – no per-type {@code instanceof} chains.
 *
 * <p>Non-terminal states (e.g. {@link sk.stuba.fiit.core.states.PausedState})
 * simply replace {@link #currentState} in-place.
 *
 * <h2>Debug shortcut</h2>
 * <p>Pressing {@code F1} toggles hitbox outlines via
 * {@link GameRenderer#toggleDebugHitboxes()}.
 */
public class GameScreen implements Screen {

    private final AppController    app;
    private       IGameState       currentState;
    private final GameManager      gameManager;
    private final CollisionManager collisionManager;
    private final PlayerController playerController;
    private final GameRenderer     gameRenderer;

    /**
     * Constructs the screen, initialises all subsystems, and enters
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
     * <p>Terminal states implement {@link TerminalState} and carry their own
     * navigation logic – {@code GameScreen} has no knowledge of specific state
     * types beyond this single interface check.
     *
     * <p>Non-terminal states simply become the new {@link #currentState}
     * (e.g. pause / resume transitions).
     *
     * @param next the next state returned by {@link IGameState#next()}
     */
    private void handleTransition(IGameState next) {
        if (next instanceof TerminalState) {
            int level = gameManager.getCurrentLevel() != null
                ? gameManager.getCurrentLevel().getLevelNumber()
                : 1;
            ((TerminalState) next).execute(app, level, gameManager.getMaxLevels());
        } else {
            currentState = next;
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

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
