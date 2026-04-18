package sk.stuba.fiit.core;

import org.slf4j.Logger;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.exceptions.GameStateException;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.projectiles.ProjectilePool;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

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

    public void initGame() {
        Knight knight = new Knight(new Vector2D(0, 0)); // pozicia sa nastavi z Tiled
        inventory.addCharacter(knight);
    }

    public void resetGame() {
        log.info("Game reset");
        inventory = new Inventory();
        currentLevel = null;
        AtlasCache.getInstance().dispose();
        ProjectilePool.getInstance().clearAll();
        log.info("ProjectilePool cleared on reset");
    }

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
