package sk.stuba.fiit.core;

import sk.stuba.fiit.core.exceptions.SaveException;

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

    /** Resets all game states and navigates to the main menu. */
    void goToMainMenu();

    /** Starts a fresh game session and navigates to the inventory screen for level 1. */
    void startNewGame();

    /** Loads the last save file and resumes the game from the saved level. */
    void continueGame();

    /**
     * Starts the given level and transitions to the game screen.
     *
     * @param level 1-based level number to start
     */
    void startGame(int level);

    /**
     * Revives the party and navigates to the inventory screen so the player
     * can adjust their loadout before retrying the failed level.
     *
     * @param level 1-based number of the level to retry
     */
    void retryLevel(int level);

    /**
     * Navigates to the inventory management screen for the given level.
     *
     * @param level 1-based level number that will start once the player confirms
     */
    void goToInventory(int level);

    /** Navigates to the win screen after the player clears the final level. */
    void goToWinScreen();

    void goToGameOverScreen(int level);

    /** Exits the application. */
    void exitApp();

    /**
     * Persists the current game state to disk.
     *
     * <p>Returns {@code true} on success, {@code false} on failure.
     * A boolean result keeps callers free from importing
     * {@link SaveException}.
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
