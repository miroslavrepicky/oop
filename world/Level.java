package sk.stuba.fiit.world;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.physics.MovementResolver;
import sk.stuba.fiit.core.engine.Updatable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.Armour;
import sk.stuba.fiit.items.EggProjectileSpawner;
import sk.stuba.fiit.items.HealingPotion;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.EggProjectile;
import sk.stuba.fiit.core.Poolable;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.save.SaveData;
import sk.stuba.fiit.util.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents one game level and owns all active game objects within it.
 *
 * <h2>Loading</h2>
 * <p>{@link #load(String, PlayerCharacter)} delegates to {@link MapManager} to parse a Tiled {@code .tmx} file.
 * Entity spawn data extracted from the {@code "entities"} layer is converted into live objects:
 * enemies (with AI and movement resolvers), ducks, and ground items. The active player's
 * position is set from the {@code "player"} entity.
 *
 * <h2>Update loop</h2>
 * <p>{@link #update(UpdateContext)} advances one frame:
 * <ol>
 *   <li>Inactive poolable projectiles are returned to {@link sk.stuba.fiit.projectiles.ProjectilePool}.</li>
 *   <li>Inactive projectiles are removed from the list.</li>
 *   <li>Active projectiles are updated.</li>
 *   <li>Enemies whose death animation has finished are removed.</li>
 *   <li>Remaining enemies are updated (AI, physics).</li>
 *   <li>{@link EggProjectileSpawner} marker items are converted into live {@link EggProjectile}s.</li>
 *   <li>Remaining items are updated.</li>
 *   <li>Dead ducks are removed; living ducks are updated.</li>
 *   <li>The level completion flag is set when all enemies are dead.</li>
 * </ol>
 *
 * <h2>On-hit effects</h2>
 * <p>DOT and slow effects are handled directly on {@link Character} instances and
 * ticked there – no separate status-effect list is maintained in the level.
 */
public class Level implements Updatable {
    private int levelNumber;
    private List<EnemyCharacter> enemies;
    private List<Item>           items;
    private List<Duck>           ducks;

    /** Set to {@code true} once all enemies have been defeated. */
    private boolean              isCompleted;
    private List<Projectile>     projectiles = new ArrayList<>();
    private MapManager           mapManager;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.enemies     = new ArrayList<>();
        this.items       = new ArrayList<>();
        this.ducks       = new ArrayList<>();
        this.isCompleted = false;
    }

    /** Adds a projectile to the active projectile list. */
    public void addProjectile(Projectile projectile) { projectiles.add(projectile); }

    /**
     * Loads the Tiled map from {@code mapPath} and spawns all entities defined
     * in the {@code "entities"} layer.
     *
     * <p>Supported entity types: {@code player}, {@code enemy_knight}, {@code duck},
     * {@code healing_potion}, {@code armour}, {@code enemy_archer}, {@code enemy_wizzard},
     * {@code dark_knight}.
     *
     * @param mapPath relative path to the {@code .tmx} file
     */
    public void load(String mapPath, PlayerCharacter active) {
        mapManager = new MapManager(mapPath);
        for (Map<String, Object> entity : mapManager.getEntities()) {
            String type = (String) entity.get("type");
            float  x    = (float) entity.get("x");
            float  y    = (float) entity.get("y");

            switch (type) {
                case "player":
                    if (active != null) active.setPosition(new Vector2D(x, y));
                    break;
                case "enemy_knight":
                    EnemyKnight ek = new EnemyKnight(new Vector2D(x, y));
                    ek.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y), 52f, 52f);
                    ek.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                    spawnEnemy(ek);
                    break;
                case "duck":
                    addDuck(new Duck(new Vector2D(x, y)));
                    break;
                case "healing_potion":
                    addItem(new HealingPotion(50, new Vector2D(x, y)));
                    break;
                case "armour":
                    addItem(new Armour(50, new Vector2D(x, y)));
                    break;
                case "enemy_archer":
                    EnemyArcher ea = new EnemyArcher(new Vector2D(x, y));
                    ea.initAI(new Vector2D(x - 150, y), new Vector2D(x + 150, y), 300f, 250f);
                    ea.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                    spawnEnemy(ea);
                    break;
                case "enemy_wizzard":
                    EnemyWizzard ew = new EnemyWizzard(new Vector2D(x, y));
                    ew.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y), 350f, 280f);
                    ew.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                    spawnEnemy(ew);
                    break;
                case "dark_knight":
                    DarkKnight dk = new DarkKnight(new Vector2D(x, y));
                    dk.initAI(new Vector2D(x - 200, y), new Vector2D(x + 200, y), 200f, 150f);
                    dk.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                    spawnEnemy(dk);
                    break;
            }
        }
    }

    /**
     * Advances all level objects by one frame. See class-level documentation for the
     * full update order.
     *
     * @param upx the frame context; {@code upx.deltaTime} and {@code upx.platforms} are
     *            replaced internally with map-sourced data; all other fields are forwarded
     */
    @Override
    public void update(UpdateContext upx) {
        float deltaTime = upx.deltaTime;
        List<Rectangle> platforms = (mapManager != null)
            ? mapManager.getHitboxes()
            : Collections.emptyList();

        UpdateContext ctx = new UpdateContext(deltaTime, platforms, this, upx.player, upx.inventory);

        returnInactiveProjectilesToPool();
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile p : projectiles) p.update(ctx);

        enemies.removeIf(e -> !e.isAlive() && e.isDeathAnimationDone());
        for (EnemyCharacter e : enemies) e.update(ctx);

        Iterator<Item> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            if (item instanceof EggProjectileSpawner) {
                projectiles.add(new EggProjectile(item.getPosition()));
                itemIter.remove();
                continue;
            }
            item.update(ctx);
        }

        ducks.removeIf(d -> !d.isAlive());
        for (Duck d : ducks) d.update(ctx);

        if (!isCompleted && !enemies.isEmpty()
            && enemies.stream().noneMatch(Character::isAlive)) {
            isCompleted = true;
        }
    }

    /**
     * Returns all inactive poolable projectiles to their respective {@link sk.stuba.fiit.projectiles.ProjectilePool}.
     * Called at the start of each update before the inactive projectiles are removed.
     */
    private void returnInactiveProjectilesToPool() {
        for (Projectile p : projectiles) {
            if (!p.isActive() && p instanceof Poolable) {
                ((Poolable) p).returnToPool();
            }
        }
    }

    /**
     * Načíta mapu (len geometriu + hitboxy) a obnoví entity zo SaveData
     * namiesto spawnovania z TMX vrstvy "entities".
     */
    public void loadFromSave(String mapPath, SaveData savedState, PlayerCharacter active) {
        mapManager = new MapManager(mapPath);

        if (active != null) {
            for (SaveData.CharacterData cd : savedState.characters) {
                if (cd.isActive) {
                    active.setPosition(new Vector2D(cd.x, cd.y));
                    active.setFacingRight(cd.facingRight);
                    active.updateHitbox();
                    break;
                }
            }
        }

        // Nepriatelia zo save
        for (SaveData.EnemyData ed : savedState.enemies) {
            EnemyCharacter enemy = createEnemyFromSave(ed);
            if (enemy != null) spawnEnemy(enemy);
        }

        for (SaveData.DuckData dd : savedState.ducks) {
            Duck duck = new Duck(new Vector2D(dd.x, dd.y));
            duck.restoreStats(dd.hp, duck.getMaxHp()); // hp sa obnoví
            addDuck(duck);
        }

        // Predmety na zemi zo save
        for (SaveData.GroundItemData gd : savedState.groundItems) {
            Item item = createGroundItemFromSave(gd);
            if (item != null) addItem(item);
        }
    }

    private EnemyCharacter createEnemyFromSave(SaveData.EnemyData ed) {
        Vector2D pos = new Vector2D(ed.x, ed.y);
        EnemyCharacter enemy;

        switch (ed.type) {
            case "EnemyKnight": {
                EnemyKnight ek = new EnemyKnight(pos);
                ek.initAI(new Vector2D(ed.x - 100, ed.y), new Vector2D(ed.x + 100, ed.y), 52f, 52f);
                ek.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                enemy = ek;
                break;
            }
            case "EnemyArcher": {
                EnemyArcher ea = new EnemyArcher(pos);
                ea.initAI(new Vector2D(ed.x - 150, ed.y), new Vector2D(ed.x + 150, ed.y), 300f, 250f);
                ea.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                enemy = ea;
                break;
            }
            case "EnemyWizzard": {
                EnemyWizzard ew = new EnemyWizzard(pos);
                ew.initAI(new Vector2D(ed.x - 100, ed.y), new Vector2D(ed.x + 100, ed.y), 350f, 280f);
                ew.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                enemy = ew;
                break;
            }
            case "DarkKnight": {
                DarkKnight dk = new DarkKnight(pos);
                dk.initAI(new Vector2D(ed.x - 200, ed.y), new Vector2D(ed.x + 200, ed.y), 200f, 150f);
                dk.setMovementResolver(new MovementResolver(mapManager.getHitboxes()));
                enemy = dk;
                break;
            }
            default:
                return null;
        }

        enemy.restoreStats(ed.hp, ed.armor);
        return enemy;
    }

    private Item createGroundItemFromSave(SaveData.GroundItemData gd) {
        Vector2D pos = new Vector2D(gd.x, gd.y);
        switch (gd.type) {
            case "HealingPotion": return new HealingPotion(50, pos);
            case "Armour":        return new Armour(50, pos);
            default:
                return null;
        }
    }

    public void spawnEnemy(EnemyCharacter enemy) { enemies.add(enemy); }
    public void addItem(Item item)               { items.add(item); }
    public void addDuck(Duck duck)               { ducks.add(duck); }

    public List<EnemyCharacter> getEnemies()    { return enemies; }
    public List<Item>           getItems()       { return items; }
    public List<Duck>           getDucks()       { return ducks; }
    public boolean              isCompleted()    { return isCompleted; }
    public int                  getLevelNumber() { return levelNumber; }
    public List<Projectile>     getProjectiles() { return projectiles; }
    public MapManager           getMapManager()  { return mapManager; }
}
