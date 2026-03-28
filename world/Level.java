package sk.stuba.fiit.world;

import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.items.EggProjectileSpawner;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;
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
        this.enemies     = new ArrayList<>();
        this.items       = new ArrayList<>();
        this.ducks       = new ArrayList<>();
        this.isCompleted = false;
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public void load(String mapPath) {
        mapManager = new MapManager(mapPath);
        for (Map<String, Object> entity : mapManager.getEntities()) {
            String type = (String) entity.get("type");
            float x = (float) entity.get("x");
            float y = (float) entity.get("y");

            switch (type) {
                case "player":
                    PlayerCharacter active = GameManager.getInstance()
                        .getInventory().getActive();
                    if (active != null) active.setPosition(new Vector2D(x, y));
                    break;
                case "enemy_knight":
                    EnemyKnight enemy = new EnemyKnight(new Vector2D(x, y));
                    enemy.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y));
                    spawnEnemy(enemy);
                    break;
                case "duck":
                    addDuck(new Duck(new Vector2D(x, y)));
                    break;
                case "healing_potion":
                    addItem(new HealingPotion(50, new Vector2D(x, y)));
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
        // --- projektily ---
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile projectile : projectiles) {
            projectile.update(deltaTime);
        }

        // --- nepriatelia ---
        enemies.removeIf(e -> !e.isAlive());
        for (EnemyCharacter enemy : enemies) {
            enemy.update(deltaTime);
        }

        // --- items ---
        // Spracuj EggProjectileSpawner markery:
        //   keď CollisionManager pridá marker do inventára cez duck.onKilled(),
        //   CollisionManager v skutočnosti volá addItem() tu.
        // Tu prechádzame items a spawnujeme EggProjectile namiesto markera.
        Iterator<Item> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            if (item instanceof EggProjectileSpawner) {
                EggProjectile egg = new EggProjectile(item.getPosition());
                projectiles.add(egg);
                itemIter.remove();
                continue;
            }
            item.update(deltaTime);
        }

        // --- kacky ---
        ducks.removeIf(d -> !d.isAlive());
        for (Duck duck : ducks) {
            duck.update(deltaTime);
        }

        checkCompletion();
    }

    // ---------------------------------------------------------------
    // POZNAMKA k duck drop flow:
    //
    //  CollisionManager.checkPlayerVsDucks():
    //    duck.takeDamage(duck.getHp());
    //    Item result = duck.onKilled();          // FriendlyDuck alebo EggProjectileSpawner
    //    level.addItem(result);                  // <- uloz do levelu, NIE do inventara
    //    level.getDucks().remove(duck);
    //
    //  Level.update() potom:
    //    ak result je EggProjectileSpawner -> spawn EggProjectile, odober item
    //    ak result je FriendlyDuck         -> zostane v items; hráč si ho zoberie
    //
    //  Zmeňte CollisionManager.checkPlayerVsDucks() podľa toho:
    //    // starý kód: player.getInventory().addItem(result);
    //    // nový kód:  level.addItem(result);
    // ---------------------------------------------------------------

    public boolean checkCompletion() {
        isCompleted = enemies.stream().allMatch(e -> !e.isAlive());
        return isCompleted;
    }

    public void spawnEnemy(EnemyCharacter enemy) { enemies.add(enemy); }
    public void addItem(Item item)               { items.add(item); }
    public void addDuck(Duck duck)               { ducks.add(duck); }

    public List<EnemyCharacter> getEnemies()   { return enemies; }
    public List<Item>           getItems()      { return items; }
    public List<Duck>           getDucks()      { return ducks; }
    public boolean              isCompleted()   { return isCompleted; }
    public int                  getLevelNumber(){ return levelNumber; }
    public List<Projectile>     getProjectiles(){ return projectiles; }
    public MapManager           getMapManager() { return mapManager; }
}
