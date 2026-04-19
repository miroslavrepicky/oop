package sk.stuba.fiit.core;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.core.exceptions.GameStateException;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;


/**
 * Central singleton coordinating top-level game state.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Holds the active {@link Inventory} (party + items).</li>
 *   <li>Creates and loads {@link Level} instances via {@link #startLevel(int)}.</li>
 *   <li>Initialises a new game with a default {@code Knight} party via {@link #initGame()}.</li>
 *   <li>Resets all game states (inventory, level, caches) via {@link #resetGame()}.</li>
 *   <li>Revives all party members after a game-over retry.</li>
 * </ul>
 *
 * <p>Screens and controllers access the manager via {@link #getInstance()}.
 * Game logic classes (enemies, items, attacks) receive their dependencies through
 * {@link UpdateContext} or method parameters to avoid hard coupling to this singleton.
 */
public class GameManager {
    private static GameManager instance;
    private Inventory inventory;
    private Level currentLevel;
    private static final int MAX_LEVELS = 1;
    private static final Logger log = GameLogger.get(GameManager.class);

    private GameManager() {
        inventory = new Inventory();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Starts the specified level: creates a new {@link Level}, loads the Tiled map,
     * and positions the active player at the spawn point defined in the map.
     *
     * @param levelNumber 1-based level number; must be in range [1, {@link #MAX_LEVELS}]
     * @throws GameStateException if no active player exists or the level number is invalid
     */
    public void startLevel(int levelNumber) {
        if (inventory.getActive() == null) {
            log.error("Cannot start level – no active player: level={}", levelNumber);
            throw new GameStateException(
                "Cannot start level – no active player character",
                "GameManager.startLevel");
        }
        if (levelNumber < 1 || levelNumber > MAX_LEVELS) {
            log.error("Invalid level number: level={}, max={}", levelNumber, MAX_LEVELS);
            throw new GameStateException(
                "Invalid level number: " + levelNumber,
                "GameManager.startLevel");
        }
        log.info("Starting level: level={}", levelNumber);
        this.currentLevel = new Level(levelNumber);
        this.currentLevel.load("test_map.tmx");
        log.info("Level loaded: level={}, enemies={}, items={}",
            levelNumber,
            currentLevel.getEnemies().size(),
            currentLevel.getItems().size());
    }

    /**
     * Initialises a new game session with a default {@code Knight} as the base character.
     * Called after {@link #resetGame()} when starting a new game.
     */
    public void initGame() {
        Knight knight = new Knight(new Vector2D(0, 0)); // pozicia sa nastavi z Tiled
        inventory.addCharacter(knight);
    }

    /**
     * Resets all game state: clears the inventory, nulls the current level,
     * disposes the {@link AtlasCache} and empties the {@link ProjectilePool}.
     * Must be called before starting a new game or loading a save.
     */
    public void resetGame() {
        log.info("Game reset");
        inventory = new Inventory();
        currentLevel = null;
        AtlasCache.getInstance().dispose();
        ProjectilePool.getInstance().clearAll();
        log.info("ProjectilePool cleared on reset");
    }

    /**
     * Restores all party members to full HP. Called by {@code GameOverScreen}
     * when the player chooses to retry the level.
     */
    public void reviveParty() {
        inventory.getCharacters().forEach(c -> {
            c.revive();
            log.info("Character revived: name={}", c.getName());
        });
    }

    public Inventory getInventory() { return inventory; }
    public Level getCurrentLevel() { return currentLevel; }
    public int getMaxLevels() { return MAX_LEVELS; }
}
