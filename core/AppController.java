package sk.stuba.fiit.core;

/**
 * Application-level controller contract that mediates between UI screens and
 * the game's business logic.
 *
 * <p>Screens know only {@code AppController}. All navigation and the business
 * logic that must happen before navigation live in {@link ShadowQuest}.
 * Screens never call {@link GameManager} or {@link sk.stuba.fiit.save.SaveManager}
 * for mutations; they may read data through {@link GameManager} for display.
 */
public interface AppController {

    void goToMainMenu();
    void startNewGame();
    void continueGame();
    void startGame(int level);
    void retryLevel(int level);
    void goToInventory(int level);
    void goToWinScreen();
    void exitApp();

    /**
     * Persists the current game state to disk.
     *
     * <p>Returns {@code true} on success, {@code false} on failure.
     * A boolean result keeps callers free from importing
     * {@link sk.stuba.fiit.save.SaveManager.SaveException}.
     *
     * @param level 1-based level number to record in the save file
     * @return {@code true} if saved successfully
     */
    boolean saveGame(int level);

    /**
     * Returns {@code true} if a save file exists on disk.
     *
     * <p>Routing this through the controller keeps
     * {@link sk.stuba.fiit.ui.MainMenuScreen} free from importing
     * {@link sk.stuba.fiit.save.SaveManager}.
     */
    boolean hasSave();
}
