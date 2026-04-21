package sk.stuba.fiit.core;

/**
 * Application-level controller contract that mediates between UI screens and
 * the game's business logic.
 *
 * <h2>Motivation</h2>
 * <p>Before this interface existed, every {@code Screen} imported both
 * {@link ShadowQuest} (to call {@code setScreen()}) and {@link GameManager}
 * (to call {@code resetGame()}, {@code reviveParty()}, {@code startLevel()},
 * etc.). That means View classes were directly invoking business logic –
 * a clear MVC violation.
 *
 * <p>After the refactor:
 * <ul>
 *   <li>Screens know only {@code AppController} – a stable interface.</li>
 *   <li>All navigation <em>and</em> the business logic that must happen
 *       before navigation live in {@link ShadowQuest}, which implements
 *       this interface.</li>
 *   <li>Screens never touch {@link GameManager} for mutations; they may
 *       still read data through it for display purposes
 *       (e.g. {@code getInventory()}).</li>
 * </ul>
 *
 * <h2>Rule of thumb</h2>
 * <p>If a screen needs to <em>change</em> game state before switching screens,
 * that belongs here. If a screen only needs to <em>read</em> game state for
 * rendering, it may call {@link GameManager} directly.
 */
public interface AppController {

    /**
     * Resets all game state and navigates to the main menu screen.
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.GameOverScreen},
     * {@link sk.stuba.fiit.ui.WinScreen}.
     */
    void goToMainMenu();

    /**
     * Deletes any existing save file, resets the game state, creates a default
     * {@link sk.stuba.fiit.characters.Knight} party, and navigates to the
     * inventory screen for level 1.
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.MainMenuScreen}.
     */
    void startNewGame();

    /**
     * Loads the save file, starts the saved level, and navigates to the game
     * screen. Falls back to {@link #startNewGame()} if loading fails or no
     * save file is present.
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.MainMenuScreen}.
     */
    void continueGame();

    /**
     * Starts the given level and navigates to the game screen.
     *
     * <p>This method is the single place that calls
     * {@link GameManager#startLevel(int)} so no screen ever has to.
     *
     * @param level 1-based level number to start
     * @throws sk.stuba.fiit.core.exceptions.GameStateException if the level
     *         number is out of range or no active player exists
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.InventoryScreen}.
     */
    void startGame(int level);

    /**
     * Revives all party members and navigates to the inventory screen for the
     * given level so the player can adjust their loadout before retrying.
     *
     * @param level 1-based level number that was just failed
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.GameOverScreen}.
     */
    void retryLevel(int level);

    /**
     * Navigates to the inventory screen for the given level without starting
     * the level yet. Used when the player completes a level and should prepare
     * their loadout before the next one.
     *
     * @param level 1-based level number for which the inventory is being shown
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.GameScreen} on
     * {@link sk.stuba.fiit.core.states.LevelCompleteState}.
     */
    void goToInventory(int level);

    /**
     * Navigates to the win screen shown after the final level is completed.
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.GameScreen} when the
     * completed level number exceeds the maximum.
     */
    void goToWinScreen();

    /**
     * Terminates the application.
     *
     * <p>Typical callers: {@link sk.stuba.fiit.ui.MainMenuScreen}.
     */
    void exitApp();
}
