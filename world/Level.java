package sk.stuba.fiit.world;

import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Level implements Updatable {
    private int levelNumber;
    private List<EnemyCharacter> enemies;
    private List<Item> items;
    private List<Duck> ducks;
    private boolean isCompleted;
    private List<Projectile> projectiles = new ArrayList<>();
    private MapManager mapManager;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        this.ducks = new ArrayList<>();
        this.isCompleted = false;
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public void load(String mapPath) {
        // načítanie mapy, nepriateľov, predmetov
        mapManager = new MapManager(mapPath);
        for (Map<String, Object> entity : mapManager.getEntities()) {
            String type = (String) entity.get("type");
            float x = (float) entity.get("x");
            float y = (float) entity.get("y");

            switch (type) {
                case "player":
                    PlayerCharacter active = GameManager.getInstance()
                        .getInventory()
                        .getActive();
                    if (active != null) {
                        active.setPosition(new Vector2D(x, y));
                    }
                    break;
                case "enemy_knight":
                    EnemyKnight enemy = new EnemyKnight(new Vector2D(x, y));
                    enemy.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y));
                    spawnEnemy(enemy);
                    break;
                case "duck":
                    Duck duck = new Duck(new Vector2D(x, y));
                    addDuck(duck);
                    break;
                case "healing_potion":
                    HealingPotion potion = new HealingPotion(50, new Vector2D(x, y));
                    addItem(potion);
                    break;
                case "enemy_archer":
                    EnemyArcher enemyArcher = new EnemyArcher(new Vector2D(x, y));
                    enemyArcher.initAI(new Vector2D(x - 150, y), new Vector2D(x + 150, y));
                    spawnEnemy(enemyArcher);
                    break;
                case "enemy_wizzard":
                    EnemyWizzard enemyWizzard = new EnemyWizzard(new Vector2D(x, y));
                    enemyWizzard.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y));
                    spawnEnemy(enemyWizzard);
                    break;
                case "dark_knight":
                    DarkKnight darkKnight = new DarkKnight(new Vector2D(x, y));
                    darkKnight.initAI(new Vector2D(x - 200, y), new Vector2D(x + 200, y));
                    spawnEnemy(darkKnight);
                    break;

            }
        }
    }

    @Override
    public void update(float deltaTime) {
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile projectile : projectiles) {
            projectile.update(deltaTime);
        }
        // odstran mrtvych nepriatelov
        enemies.removeIf(enemy -> !enemy.isAlive());
        ducks.removeIf(duck -> !duck.isAlive());
        for (EnemyCharacter enemy : enemies) {
            enemy.update(deltaTime);
        }
        for (Item item : items) {
            item.update(deltaTime);
        }
        for (Duck duck : ducks) {
            duck.update(deltaTime);
        }
        checkCompletion();
    }

    public boolean checkCompletion() {
        isCompleted = enemies.stream().allMatch(e -> !e.isAlive());
        return isCompleted;
    }

    public void spawnEnemy(EnemyCharacter enemy) {
        enemies.add(enemy);
    }

    public void addItem(Item item) { items.add(item); }
    public void addDuck(Duck duck) { ducks.add(duck); }

    public List<EnemyCharacter> getEnemies() { return enemies; }
    public List<Item> getItems() { return items; }
    public List<Duck> getDucks() { return ducks; }
    public boolean isCompleted() { return isCompleted; }
    public int getLevelNumber() { return levelNumber; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public MapManager getMapManager() { return mapManager; }
}
