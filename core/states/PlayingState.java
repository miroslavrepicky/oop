package sk.stuba.fiit.core.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.physics.CollisionManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.render.RenderSnapshot;
import sk.stuba.fiit.core.PlayerController;
import sk.stuba.fiit.world.Level;

/**
 * Stav: hra beží – hráč sa pohybuje, AI útočí, kolízie sa riešia.
 *
 * Zmena oproti pôvodnému kódu:
 *  - render() zostavuje {@link RenderSnapshot} a predáva ho do
 *    {@link GameRenderer#render(RenderSnapshot, float)}.
 *    GameRenderer tak nemusí volať GameManager sám.
 *  - nearbyItemAvailable sa číta z CollisionManager tu (high-level stav)
 *    a predáva sa do snapshotu – HUDRenderer o CollisionManager nevie.
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

    // -------------------------------------------------------------------------
    //  Update
    // -------------------------------------------------------------------------

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            nextState = new PausedState(gameRenderer, this);
            return;
        }

        playerController.update(deltaTime);

        Level level = gameManager.getCurrentLevel();
        if (level != null) {
            level.update(new UpdateContext(deltaTime));
        }
        collisionManager.update(level);

        if (gameManager.getInventory().isPartyDefeated()) {
            nextState = new GameOverDelayState(
                gameManager, gameRenderer, GameOverDelayState.DELAY);
            return;
        }

        if (level != null && level.isCompleted()) {
            int nextLevel = level.getLevelNumber() + 1;
            nextState = (nextLevel > gameManager.getMaxLevels())
                ? new WinState()
                : new LevelCompleteState(gameManager);
        }
    }

    // -------------------------------------------------------------------------
    //  Render – zostavíme snapshot tu, nie v GameRenderer
    // -------------------------------------------------------------------------

    @Override
    public void render(float deltaTime) {
        Level level = gameManager.getCurrentLevel();
        if (level == null) return;

        PlayerCharacter player = gameManager.getInventory().getActive();
        boolean nearbyItem = collisionManager.getNearbyItem() != null;

        RenderSnapshot snapshot = new RenderSnapshot(
            player,
            level.getEnemies(),
            level.getDucks(),
            level.getItems(),
            level.getProjectiles(),
            level.getMapManager(),
            gameRenderer.isDebugHitboxes(),
            nearbyItem
        );

        gameRenderer.render(snapshot, deltaTime);
    }

    // -------------------------------------------------------------------------
    //  Prechod stavu
    // -------------------------------------------------------------------------

    @Override
    public IGameState next() {
        IGameState result = nextState;
        nextState = null;
        return result;
    }
}
