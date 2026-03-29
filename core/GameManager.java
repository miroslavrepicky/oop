package sk.stuba.fiit.core;


import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class GameManager {
    private static GameManager instance;
    private GameState gameState;
    private Inventory inventory;
    private Level currentLevel;
    private float gameOverTimer = 0f;
    private static final float GAME_OVER_DELAY = 3.0f;
    private static final int MAX_LEVELS = 1;


    private GameManager() {
        gameState = GameState.MENU;
        inventory = new Inventory();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startLevel(int levelNumber) {
        // obnov len level – nie inventory
        this.currentLevel = new Level(levelNumber);
        //currentLevel.load("maps/level" + levelNumber + ".tmx");

        this.currentLevel.load("test_map.tmx");
        this.gameState = GameState.PLAYING;
    }

    public void initGame() {
        Knight knight = new Knight(new Vector2D(0, 0)); // pozícia sa nastaví z Tiled
        inventory.addCharacter(knight);
        Wizzard archer = new Wizzard(new Vector2D(0, 0));
        inventory.addCharacter(archer);
        startLevel(1);
    }

    public void onPartyDefeated() {
        gameState = GameState.GAME_OVER;
        gameOverTimer = GAME_OVER_DELAY;
        for (PlayerCharacter c : inventory.getCharacters()) {
            c.revive();
        }
    }

    public void onLevelComplete() {
        startLevel(currentLevel.getLevelNumber() + 1);
        int nextLevel = currentLevel.getLevelNumber() + 1;
        if (nextLevel > MAX_LEVELS) {
            gameState = GameState.WIN;
        } else {
            startLevel(nextLevel);
        }
    }

    public void update(float deltaTime) {
        if (currentLevel != null && gameState == GameState.PLAYING) {
            currentLevel.update(deltaTime);

            if (inventory.isPartyDefeated()) {
                onPartyDefeated();
            }

            if (currentLevel.isCompleted()) {
                onLevelComplete();
            }
        }

        // čakaj pred reštartom
        if (gameState == GameState.GAME_OVER) {
            gameOverTimer -= deltaTime;
            if (gameOverTimer <= 0) {
                restartLevel();
            }
        }
    }

    private void restartLevel() {
        startLevel(currentLevel.getLevelNumber());
    }

    public Inventory getInventory() { return inventory; }
    public Level getCurrentLevel() { return currentLevel; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }
}
