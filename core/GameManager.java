package sk.stuba.fiit.core;

import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class GameManager {
    private static GameManager instance;
    private Inventory inventory;
    private Level currentLevel;
    private static final int MAX_LEVELS = 1;

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
        this.currentLevel = new Level(levelNumber);
        this.currentLevel.load("test_map.tmx");
    }

    public void initGame() {
        Knight knight = new Knight(new Vector2D(0, 0)); // pozicia sa nastavi z Tiled
        inventory.addCharacter(knight);
    }

    public void resetGame() {
        inventory = new Inventory();
        currentLevel = null;
    }

    public void reviveParty() {
        for (PlayerCharacter c : inventory.getCharacters()) {
            c.revive();
        }
    }

    public Inventory getInventory() { return inventory; }
    public Level getCurrentLevel() { return currentLevel; }
    public int getMaxLevels() { return MAX_LEVELS; }
}
