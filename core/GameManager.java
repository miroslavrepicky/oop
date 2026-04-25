package sk.stuba.fiit.core;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.core.exceptions.GameStateException;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.save.SaveData;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;


/**
 * Central singleton that coordinates top-level game state across screens and sessions.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Holds the active {@link Inventory} (party and items).</li>
 *   <li>Creates and loads {@link Level} instances via {@link #startLevel(int)}.</li>
 *   <li>Initializes a new game session with a default {@link Knight} via {@link #initGame()}.</li>
 *   <li>Resets all game states (inventory, level, texture cache, projectile pool)
 *       via {@link #resetGame()}.</li>
 *   <li>Revives all party members after a game-over retry via {@link #reviveParty()}.</li>
 * </ul>
 *
 * <h2>Design notes</h2>
 * <p>Screens and controllers access the manager via {@link #getInstance()}.
 * Game logic classes (enemies, items, attacks) receive their dependencies through
 * {@link UpdateContext} or method parameters to avoid direct coupling to this singleton.
 */
public class GameManager {
    private static GameManager instance;
    private Inventory inventory;
    private Level currentLevel;

    /** Maximum number of levels in the game. Used to determine win condition. */
    private static final int MAX_LEVELS = 1;
    private static final Logger log = GameLogger.get(GameManager.class);

    private GameManager() {
        inventory = new Inventory();
    }

    /** Returns the single shared instance, creating it on the first call. */
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
     * @throws GameStateException if no active player exists or the level number is out of range
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
        this.currentLevel.load("test_map.tmx", inventory.getActive());
        log.info("Level loaded: level={}, enemies={}, items={}",
            levelNumber,
            currentLevel.getEnemies().size(),
            currentLevel.getItems().size());
    }

    /**
     * Starts a level from a saved state: loads the map geometry and restores
     * all entities from {@link SaveData}.
     * Must be called after {@link #resetGame()} and inventory restoration.
     *
     * @param savedState complete {@link SaveData} object returned by {@code SaveManager.load()}
     */
    public void startLevelFromSave(SaveData savedState) {
        if (inventory.getActive() == null) {
            throw new GameStateException(
                "Cannot start level – no active player", "GameManager.startLevelFromSave");
        }
        int levelNumber = savedState.currentLevel;
        log.info("Starting level from save: level={}", levelNumber);
        this.currentLevel = new Level(levelNumber);
        this.currentLevel.loadFromSave("test_map.tmx", savedState, inventory.getActive());
        log.info("Level restored: level={}, enemies={}, groundItems={}",
            levelNumber, savedState.enemies.size(), savedState.groundItems.size());
    }

    /**
     * Initializes a new game session with a default {@link Knight} as the base character.
     * Must be called after {@link #resetGame()} when starting a new game.
     */
    public void initGame() {
        Knight knight = new Knight(new Vector2D(0, 0)); // Position from tiled map
        inventory.addCharacter(knight);
    }

    /**
     * Resets all game states: clears the inventory, nulls the current level,
     * disposes the {@link AtlasCache}, and empties the {@link ProjectilePool}.
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
     * Restores all party members to full HP.
     * Called by {@code GameOverScreen} when the player chooses to retry the level.
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
