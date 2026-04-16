package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

/**
 * GameScreen po refaktore na State vzor.
 *
 * Pred refaktorom:
 *   render() obsahoval sériu if (state == X) blokov, ktoré rastli
 *   s každým novým stavom (GAME_OVER, WIN, LEVEL_COMPLETE, PAUSED...).
 *
 * Po refaktore:
 *   render() deleguje na currentState.update() + currentState.render().
 *   Prechody stavu riadi samotný stav cez next().
 *   Pridanie nového stavu = nová trieda implementujúca IGameState,
 *   žiadna zmena v GameScreen.
 */
public class GameScreen implements Screen {

    private final ShadowQuest game;
    private IGameState        currentState;

    // Závislosí zdieľané medzi stavmi
    private final GameManager      gameManager;
    private final CollisionManager collisionManager;
    private final PlayerController playerController;
    private final GameRenderer     gameRenderer;

    public GameScreen(ShadowQuest game) {
        this.game = game;

        gameManager      = GameManager.getInstance();
        collisionManager = new CollisionManager();
        gameRenderer     = new GameRenderer();
        gameRenderer.setCollisionManager(collisionManager);
        playerController = new PlayerController(collisionManager);

        // Počiatočný stav
        currentState = new PlayingState(
            playerController, gameManager, collisionManager, gameRenderer);
    }

    @Override
    public void render(float deltaTime) {

        // --- debug klávesa (F1) nezávisí od stavu ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            gameRenderer.toggleDebugHitboxes();
        }

        // --- 1. Update aktívneho stavu ---
        currentState.update(deltaTime);

        // --- 2. Skontrolujeme prechod stavu ---
        IGameState next = currentState.next();
        if (next != null) {
            handleTransition(next);
            return; // render prebehne až v nasledujúcom frame s novým stavom
        }

        // --- 3. Render aktívneho stavu ---
        currentState.render(deltaTime);
    }

    /**
     * Centrálne miesto pre všetky prechody stavov.
     * Terminálne stavy spustia zmenu Screen; ostatné len nastavia currentState.
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
            // Interný prechod (napr. Playing → Paused → Playing)
            currentState = next;
        }
    }

    // -------------------------------------------------------------------------
    //  Screen lifecycle
    // -------------------------------------------------------------------------

    @Override public void resize(int width, int height) { gameRenderer.resize(width, height); }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        gameRenderer.dispose();
    }
}
