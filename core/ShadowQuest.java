package sk.stuba.fiit.core;

import com.badlogic.gdx.Game;
import sk.stuba.fiit.save.SaveManager;
import sk.stuba.fiit.ui.GameScreen;
import sk.stuba.fiit.ui.InventoryScreen;
import sk.stuba.fiit.ui.MainMenuScreen;
import sk.stuba.fiit.ui.WinScreen;
import sk.stuba.fiit.ui.GameOverScreen;

/**
 * LibGDX application entry point and central {@link AppController} implementation.
 *
 * <h2>Dual role</h2>
 * <ol>
 *   <li><b>LibGDX lifecycle</b> – extends {@link Game} so LibGDX calls
 *       {@link #create()}, {@link #dispose()}, etc.</li>
 *   <li><b>Application controller</b> – implements {@link AppController} so
 *       screens can trigger navigation and business-logic transitions without
 *       importing {@link GameManager} or each other.</li>
 * </ol>
 *
 * <h2>Design rationale</h2>
 * <p>Every navigation action that requires mutating game state (reset, revive,
 * startLevel…) lives here. Screens receive only an {@link AppController}
 * reference and call descriptive methods such as {@link #retryLevel(int)} –
 * they never know which concrete screen will appear next or what state changes
 * occur behind the scenes.
 *
 * <p>This is the <em>only</em> class in the project that is allowed to call
 * both {@link GameManager} mutations <em>and</em> {@code setScreen()}.
 */
public class ShadowQuest extends Game implements AppController {

    // -------------------------------------------------------------------------
    //  LibGDX lifecycle
    // -------------------------------------------------------------------------

    /**
     * Called once by LibGDX after the OpenGL context is ready.
     * Navigates immediately to the main menu.
     */
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Called by LibGDX when the application exits.
     * Releases all cached texture atlases.
     */
    @Override
    public void dispose() {
        AtlasCache.getInstance().dispose();
        super.dispose();
    }

    // -------------------------------------------------------------------------
    //  AppController – navigation + business logic
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: resets all game state via {@link GameManager#resetGame()}
     * then switches to {@link MainMenuScreen}.
     */
    @Override
    public void goToMainMenu() {
        GameManager.getInstance().resetGame();
        setScreen(new MainMenuScreen(this));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation:
     * <ol>
     *   <li>Deletes the existing save file (prevents stale continues).</li>
     *   <li>Resets all game state.</li>
     *   <li>Initialises the default {@link sk.stuba.fiit.characters.Knight} party.</li>
     *   <li>Switches to {@link InventoryScreen} for level 1.</li>
     * </ol>
     */
    @Override
    public void startNewGame() {
        SaveManager.getInstance().deleteSave();
        GameManager.getInstance().resetGame();
        GameManager.getInstance().initGame();
        setScreen(new InventoryScreen(this, 1));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: asks {@link SaveManager} to deserialise the save file
     * and reconstruct the inventory. If the returned level number is valid,
     * starts that level and switches to {@link GameScreen}. On any failure,
     * falls back to {@link #startNewGame()}.
     */
    @Override
    public void continueGame() {
        int level = SaveManager.getInstance().load();
        if (level > 0) {
            GameManager.getInstance().startLevel(level);
            setScreen(new GameScreen(this));
        } else {
            startNewGame();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: delegates level construction to
     * {@link GameManager#startLevel(int)} then switches to {@link GameScreen}.
     */
    @Override
    public void startGame(int level) {
        GameManager.getInstance().startLevel(level);
        setScreen(new GameScreen(this));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: switches to {@link InventoryScreen} for the given
     * level. No game state is mutated – the player is preparing their loadout
     * before the level starts.
     */
    @Override
    public void goToInventory(int level) {
        setScreen(new InventoryScreen(this, level));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: switches to {@link WinScreen}.
     */
    @Override
    public void goToWinScreen() {
        setScreen(new WinScreen(this));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: revives all party members via
     * {@link GameManager#reviveParty()} then switches to {@link InventoryScreen}
     * for the failed level so the player can adjust their loadout.
     */
    @Override
    public void retryLevel(int level) {
        GameManager.getInstance().reviveParty();
        setScreen(new InventoryScreen(this, level));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation: delegates to {@link com.badlogic.gdx.Gdx#app#exit()}.
     */
    @Override
    public void exitApp() {
        com.badlogic.gdx.Gdx.app.exit();
    }
}
