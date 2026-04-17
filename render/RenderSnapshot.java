package sk.stuba.fiit.render;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.MapManager;

import java.util.List;

/**
 * Nemodifikovateľný dátový objekt popisujúci stav scény v jednom snímku.
 *
 * Dôvod existencie: GameRenderer pôvodne sám volal
 * {@code GameManager.getInstance().getCurrentLevel()} – priamo ťahal
 * závislosti z globálneho singletonu. To znemožňovalo testovanie a
 * vytváralo skrytú závislosť View → GameManager → celý herný model.
 *
 * Po refaktore:
 *  - {@code Level} (alebo {@code PlayingState}) zostaví RenderSnapshot
 *    a predá ho do {@code GameRenderer.render(snapshot, deltaTime)}.
 *  - GameRenderer neimportuje Level, GameManager ani žiadnu hernú logiku.
 *  - Testovanie: stačí vytvoriť snapshot s mock dátami.
 *
 * Snapshot je read-only – obsahuje referencie, nie kópie, takže je rýchly.
 * Volajúci nesmie modifikovať listy počas renderovania.
 */
public final class RenderSnapshot {

    public final PlayerCharacter        player;
    public final List<EnemyCharacter>   enemies;
    public final List<Duck>             ducks;
    public final List<Item>             items;
    public final List<Projectile>       projectiles;
    public final MapManager             mapManager;

    /** true = zobraziť debug hitboxy (F1 klávesa). */
    public final boolean debugHitboxes;

    /** true = v blízkosti je item ktorý sa dá zdvihnúť → HUD zobrazí "[E] PICK-UP". */
    public final boolean nearbyItemAvailable;

    public RenderSnapshot(PlayerCharacter      player,
                          List<EnemyCharacter> enemies,
                          List<Duck>           ducks,
                          List<Item>           items,
                          List<Projectile>     projectiles,
                          MapManager           mapManager,
                          boolean              debugHitboxes,
                          boolean              nearbyItemAvailable) {
        this.player               = player;
        this.enemies              = enemies;
        this.ducks                = ducks;
        this.items                = items;
        this.projectiles          = projectiles;
        this.mapManager           = mapManager;
        this.debugHitboxes        = debugHitboxes;
        this.nearbyItemAvailable  = nearbyItemAvailable;
    }
}
