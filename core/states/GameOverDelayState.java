package sk.stuba.fiit.core.states;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.render.RenderSnapshot;
import sk.stuba.fiit.render.SnapshotBuilder;
import sk.stuba.fiit.world.Level;

/**
 * Transitional state shown briefly after the party is defeated.
 *
 * <p>The level continues to update (enemies animate, projectiles move) so the
 * death animation plays out naturally. After {@link #DELAY} seconds (or the
 * length of the player's death animation), transitions to {@link GameOverState}.
 *
 * <p>Rendering delegates to {@link SnapshotBuilder} – no duplication of
 * snapshot-building logic between this state and {@link PlayingState}.
 */
public class GameOverDelayState implements IGameState {

    public static final float DELAY = 3.0f;

    private final GameManager  gameManager;
    private final GameRenderer gameRenderer;
    private float              timer;
    private IGameState         nextState = null;

    public GameOverDelayState(GameManager gameManager,
                              GameRenderer gameRenderer,
                              float delay) {
        this.gameManager  = gameManager;
        this.gameRenderer = gameRenderer;
        this.timer        = delay;
    }

    @Override
    public void update(float deltaTime) {
        UpdateContext ctx = new UpdateContext(deltaTime);
        Level level = gameManager.getCurrentLevel();
        if (level != null) {
            level.update(ctx);
        }

        PlayerCharacter player = gameManager.getInventory().getActive();
        if (player != null) {
            player.updateAnimation(ctx);
        }

        timer -= deltaTime;
        if (timer <= 0f) {
            nextState = new GameOverState();
        }
    }

    @Override
    public void render(float deltaTime) {
        Level level = gameManager.getCurrentLevel();
        if (level == null) return;

        PlayerCharacter player = gameManager.getInventory().getActive();

        RenderSnapshot snapshot = SnapshotBuilder.build(
            player, level, gameRenderer.isDebugHitboxes(), false);

        gameRenderer.render(snapshot, deltaTime);
    }

    @Override
    public IGameState next() {
        return nextState;
    }
}
