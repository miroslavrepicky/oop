package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AppController;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.render.RenderSnapshot;
import sk.stuba.fiit.render.SnapshotBuilder;
import sk.stuba.fiit.core.PlayerController;
import sk.stuba.fiit.world.Level;

/**
 * Active gameplay state: player input is processed, AI advances, and collisions
 * are resolved every frame.
 *
 * <p>Rendering uses {@link SnapshotBuilder} to convert live model objects into a
 * {@link RenderSnapshot} DTO. {@link GameRenderer} never receives model classes directly,
 * preserving the MVC separation.
 *
 * <h2>Transition triggers</h2>
 * <ul>
 *   <li>{@code P} key              → {@link PausedState} (game freezes, scene stays visible)</li>
 *   <li>All party members dead     → {@link GameOverDelayState} (death animation plays out)</li>
 *   <li>Level complete, more levels remain → {@link LevelCompleteState}</li>
 *   <li>Level complete, no more levels     → {@link WinState}</li>
 * </ul>
 */
public class PlayingState implements IGameState {

    private final PlayerController playerController;
    private final GameManager      gameManager;
    private final CollisionManager collisionManager;
    private final GameRenderer     gameRenderer;
    private final AppController app;

    /** Pending state to transition to; set inside {@link #update(float)}, consumed by {@link #next()}. */
    private IGameState nextState = null;

    /**
     * @param playerController handles keyboard input and applies it to the active character
     * @param gameManager      central singleton providing inventory, level, and reset logic
     * @param collisionManager detects and resolves projectile/enemy/item collisions
     * @param gameRenderer     renders the scene from a {@link RenderSnapshot} DTO
     */
    public PlayingState(PlayerController playerController,
                        GameManager      gameManager,
                        CollisionManager collisionManager,
                        GameRenderer     gameRenderer,
                        AppController    app) {
        this.playerController = playerController;
        this.gameManager      = gameManager;
        this.collisionManager = collisionManager;
        this.gameRenderer     = gameRenderer;
        this.app              = app;
    }

    /**
     * Advances one frame of gameplay:
     * <ol>
     *   <li>Checks for the pause key.</li>
     *   <li>Processes player input via {@link PlayerController#update(float)}.</li>
     *   <li>Updates the current level (enemies, projectiles, items, ducks).</li>
     *   <li>Resolves collisions via {@link CollisionManager#update(Level, PlayerCharacter)}.</li>
     *   <li>Evaluates win/loss conditions and schedules the appropriate next state.</li>
     * </ol>
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            nextState = new PausedState(gameRenderer, this, app, gameManager);
            return;
        }

        playerController.update(deltaTime);

        Level level = gameManager.getCurrentLevel();
        PlayerCharacter player = gameManager.getInventory().getActive();
        if (level != null) {
            UpdateContext ctx = new UpdateContext(
                deltaTime, null, level, player,
                gameManager.getInventory());

            level.update(ctx);
        }
        collisionManager.update(level, player);

        if (gameManager.getInventory().isPartyDefeated()) {
            float deathDuration = GameOverDelayState.DELAY;
            if (player != null && player.getAnimationManager() != null
                && player.getAnimationManager().hasAnimation("death")) {
                deathDuration = player.getAnimationManager().getAnimationDuration("death");
            }
            nextState = new GameOverDelayState(gameManager, gameRenderer, deathDuration);
            return;
        }

        if (level != null && level.isCompleted()) {
            int nextLevel = level.getLevelNumber() + 1;
            nextState = (nextLevel > gameManager.getMaxLevels())
                ? new WinState()
                : new LevelCompleteState(gameManager);
        }
    }

    /**
     * Builds a {@link RenderSnapshot} from the current model state and passes it to
     * {@link GameRenderer}. No model classes are passed to the renderer directly.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    @Override
    public void render(float deltaTime) {
        Level           level   = gameManager.getCurrentLevel();
        Inventory inv     = gameManager.getInventory();
        PlayerCharacter player  = inv.getActive();
        boolean         nearby  = collisionManager.getNearbyItem() != null;

        // Controller zostaví DTO – GameRenderer model nevidí
        RenderSnapshot snapshot = SnapshotBuilder.build(
            player, level, inv, gameRenderer.isDebugHitboxes(), nearby);

        gameRenderer.render(snapshot, deltaTime);
    }

    /**
     * Returns the pending next state and resets the field to {@code null}.
     * Called once per frame by {@code GameScreen} after {@link #update(float)}.
     */
    @Override
    public IGameState next() {
        IGameState result = nextState;
        nextState = null;
        return result;
    }
}
