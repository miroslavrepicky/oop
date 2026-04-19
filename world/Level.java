package sk.stuba.fiit.world;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.attacks.StatusEffect;
import sk.stuba.fiit.characters.*;
import sk.stuba.fiit.characters.Character;
import sk.stuba.fiit.core.GameManager;
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
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.attacks.FreezeSpellDecorator.FreezeEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Represents a single game level containing all active game objects.
 *
 * <p>Manages enemies, items, ducks, projectiles and status effects.
 * The {@link #update(UpdateContext)} method builds a shared context once
 * and distributes it to all sub-systems so no class needs to call
 * {@code GameManager} to obtain the level, player or platforms.
 *
 * <p>Level completion is detected when all spawned enemies have been killed.
 * The flag is set once and read by {@code PlayingState}.
 */
public class Level implements Updatable {
    private int levelNumber;
    private List<EnemyCharacter> enemies;
    private List<Item> items;
    private List<Duck> ducks;
    private boolean isCompleted;
    private List<Projectile> projectiles = new ArrayList<>();
    private MapManager mapManager;
    private List<StatusEffect> effects = new ArrayList<>();

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.enemies     = new ArrayList<>();
        this.items       = new ArrayList<>();
        this.ducks       = new ArrayList<>();
        this.isCompleted = false;
    }

    /**
     * Adds a projectile to the level's active projectile list.
     * Called by attack implementations ({@code ArrowAttack}, {@code SpellAttack}, etc.).
     *
     * @param projectile the projectile to add; must not be {@code null}
     */
    public void addProjectile(Projectile projectile) { projectiles.add(projectile); }

    /**
     * Loads the Tiled map and spawns all entities defined in its "entities" layer.
     * Supported entity types: {@code player}, {@code enemy_knight}, {@code enemy_archer},
     * {@code enemy_wizzard}, {@code dark_knight}, {@code duck}, {@code healing_potion},
     * {@code armour}.
     *
     * @param mapPath relative path to the {@code .tmx} Tiled map file
     */
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
                    EnemyKnight ek = new EnemyKnight(new Vector2D(x, y));
                    ek.initAI(new Vector2D(x - 100, y), new Vector2D(x + 100, y), 80f, 80f);
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


    @Override
    public void update(UpdateContext upx) {
        float deltaTime = upx.deltaTime;
        List<Rectangle> platforms = (mapManager != null)
            ? mapManager.getHitboxes()
            : Collections.emptyList();

        PlayerCharacter player = getActivePlayer();

        // Kontext sa vytvorí raz a predá všetkým – žiadna trieda
        // nevolá GameManager na získanie levelu, hráča alebo platforiem.
        UpdateContext ctx = new UpdateContext(deltaTime, platforms, this, player, null);

        returnInactiveProjectilesToPool();
        // --- projektily ---
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile p : projectiles) p.update(ctx);

        // --- nepriatelia ---
        enemies.removeIf(e -> !e.isAlive() && e.isDeathAnimationDone());
        for (EnemyCharacter e : enemies) e.update(ctx);

        // --- itemy ---
        Iterator<Item> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            if (item instanceof EggProjectileSpawner) {
                projectiles.add(new EggProjectile(item.getPosition()));
                itemIter.remove();
                continue;
            }
            item.update(ctx);   // Item.update tiež prejde na UpdateContext
        }

        // --- kačky ---
        ducks.removeIf(d -> !d.isAlive());
        for (Duck d : ducks) d.update(ctx);
        tickStatusEffects(deltaTime);

        if (!isCompleted && !enemies.isEmpty() && enemies.stream().noneMatch(Character::isAlive)) {
            isCompleted = true;
        }
    }

    /**
     * Projektil sa vracia ak implementuje {@link Poolable} –
     * žiadne {@code instanceof} checky na konkrétne typy.
     *
     * <p>EggProjectile {@link Poolable} neimplementuje, takže ho GC
     * zoberie štandardne.
     */
    private void returnInactiveProjectilesToPool() {
        for (Projectile p : projectiles) {
            if (!p.isActive() && p instanceof Poolable) {
                ((Poolable) p).returnToPool();
            }
        }
    }


    /**
     * Adds a status effect and removes any existing effect of the same type
     * targeting the same enemy (prevents double speed restoration for freeze).
     *
     * @param effect the effect to add
     */
    public void addStatusEffect(StatusEffect effect) {
        if (effect instanceof FreezeEffect) {
            effects.removeIf(e ->
                e instanceof FreezeEffect &&
                    e.getTarget() == effect.getTarget()
            );
        }
        effects.add(effect);
    }

    private void tickStatusEffects(float deltaTime) {
        for (StatusEffect effect : effects) {
            effect.tick(deltaTime);
        }
        effects.removeIf(StatusEffect::isExpired);
    }

    public PlayerCharacter getActivePlayer() {
        return GameManager.getInstance().getInventory().getActive();
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
