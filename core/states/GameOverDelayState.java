package sk.stuba.fiit.core.states;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.render.GameRenderer;
import sk.stuba.fiit.render.RenderSnapshot;
import sk.stuba.fiit.world.Level;

/**
 * Stav: krátkodobý prechod po smrti party – level ešte beží (animácia
 * smrti), ale hráč už nestráda. Po uplynutí {@link #DELAY} sekúnd
 * prejde na GameOverState.
 *
 * Zmena: render() zostavuje RenderSnapshot rovnako ako PlayingState.
 * nearbyItemAvailable = false (party je mŕtva, pick-up hint nedáva zmysel).
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
        Level level = gameManager.getCurrentLevel();
        if (level != null) {
            level.update(new UpdateContext(deltaTime));
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

        RenderSnapshot snapshot = new RenderSnapshot(
            player,
            level.getEnemies(),
            level.getDucks(),
            level.getItems(),
            level.getProjectiles(),
            level.getMapManager(),
            gameRenderer.isDebugHitboxes(),
            false   // party je mŕtva – pick-up hint nedáva zmysel
        );

        gameRenderer.render(snapshot, deltaTime);
    }

    @Override
    public IGameState next() {
        return nextState;
    }
}
