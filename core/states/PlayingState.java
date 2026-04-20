package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import sk.stuba.fiit.characters.PlayerCharacter;
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
 * Active gameplay state: player moves, AI attacks, collisions are resolved.
 *
 * <p>Rendering uses the {@link SnapshotBuilder} to build a {@link RenderSnapshot}
 * DTO from live model objects. {@link GameRenderer} never receives model classes directly.
 *
 * <p>Transition triggers:
 * <ul>
 *   <li>P key → {@link PausedState}</li>
 *   <li>All party members dead → {@link GameOverDelayState}</li>
 *   <li>Level completed, more levels remain → {@link LevelCompleteState}</li>
 *   <li>Level completed, no more levels → {@link WinState}</li>
 * </ul>
 */
public class PlayingState implements IGameState {

    private final PlayerController playerController;
    private final GameManager      gameManager;
    private final CollisionManager collisionManager;
    private final GameRenderer     gameRenderer;

    private IGameState nextState = null;

    public PlayingState(PlayerController playerController,
                        GameManager      gameManager,
                        CollisionManager collisionManager,
                        GameRenderer     gameRenderer) {
        this.playerController = playerController;
        this.gameManager      = gameManager;
        this.collisionManager = collisionManager;
        this.gameRenderer     = gameRenderer;
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            nextState = new PausedState(gameRenderer, this);
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

    @Override
    public IGameState next() {
        IGameState result = nextState;
        nextState = null;
        return result;
    }
}
