package sk.stuba.fiit.core.states;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.render.RenderSnapshot;
import sk.stuba.fiit.render.SnapshotBuilder;
import sk.stuba.fiit.world.Level;


/**
 * Transitional state that plays out naturally after the player's party is defeated.
 *
 * <p>The level continues to update (enemies animate, projectiles move) so that the
 * character's death animation completes before the screen switches to
 * {@link GameOverState}. The delay duration equals whichever is longer:
 * the player's death animation or the default {@link #DELAY} constant.
 *
 * <p>Rendering is fully delegated to {@link SnapshotBuilder}, avoiding any
 * duplication of snapshot-building logic between this state and {@link PlayingState}.
 */
public class GameOverDelayState implements IGameState {

    /**
     * Minimum delay in seconds before transitioning to {@link GameOverState}.
     * Used as a fallback when no death animation is available.
     */
    public static final float DELAY = 3.0f;
    private final GameManager  gameManager;
    private final GameRenderer gameRenderer;

    /** Countdown timer in seconds; transitions when it reaches zero. */
    private float              timer;
    private IGameState         nextState = null;

    /**
     * @param gameManager  provides the current level, inventory, and active player
     * @param gameRenderer used to render the scene while the death animation plays
     * @param delay        how many seconds to wait before transitioning; typically
     *                     the length of the player's death animation
     */
    public GameOverDelayState(GameManager gameManager,
                              GameRenderer gameRenderer,
                              float delay) {
        this.gameManager  = gameManager;
        this.gameRenderer = gameRenderer;
        this.timer        = delay;
    }

    /**
     * Continues updating the level and the player's death animation while the
     * countdown timer runs. Transitions to {@link GameOverState} when the timer expires.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    @Override
    public void update(float deltaTime) {
        PlayerCharacter player = gameManager.getInventory().getActive();
        Level level = gameManager.getCurrentLevel();
        if (level != null) {
            UpdateContext ctx = new UpdateContext(deltaTime, null, level, player, gameManager.getInventory());
            level.update(ctx);
            if (player != null) {
                player.updateAnimation(ctx);
            }
        }
        timer -= deltaTime;
        if (timer <= 0f) {
            nextState = new GameOverState();
        }
    }

    /**
     * Renders the current scene using a snapshot built by {@link SnapshotBuilder}.
     * No item pickup hint is shown during this state.
     *
     * @param deltaTime elapsed time since the last frame, in seconds
     */
    @Override
    public void render(float deltaTime) {
        Level level = gameManager.getCurrentLevel();
        if (level == null) return;

        PlayerCharacter player = gameManager.getInventory().getActive();

        RenderSnapshot snapshot = SnapshotBuilder.build(
            player, level, null, gameRenderer.isDebugHitboxes(), false);

        gameRenderer.render(snapshot, deltaTime);
    }

    /**
     * Returns the next state ({@link GameOverState}) once the timer expires,
     * or {@code null} while still counting down.
     */
    @Override
    public IGameState next() {
        return nextState;
    }
}
