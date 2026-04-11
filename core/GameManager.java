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
        Knight knight = new Knight(new Vector2D(0, 0)); // pozicia sa nastavi z Tiled
        inventory.addCharacter(knight);
    }

    public void onLevelComplete() {
        int nextLevel = currentLevel.getLevelNumber() + 1;
        if (nextLevel > MAX_LEVELS) {
            gameState = GameState.WIN;
        } else {
            gameState = GameState.LEVEL_COMPLETE;
        }
    }

    public void onPartyDefeated() {
        gameState = GameState.GAME_OVER_DELAY; // novy prechodny stav
        gameOverTimer = GAME_OVER_DELAY;
    }

    public void resetGame() {
        inventory = new Inventory();
        currentLevel = null;
        gameState = GameState.MENU;
    }

    public void reviveParty() {
        for (PlayerCharacter c : inventory.getCharacters()) {
            c.revive();
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
        if (gameState == GameState.GAME_OVER_DELAY) {
            assert currentLevel != null;
            currentLevel.update(deltaTime);
            gameOverTimer -= deltaTime;
            if (gameOverTimer <= 0) {
                gameState = GameState.GAME_OVER;
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
