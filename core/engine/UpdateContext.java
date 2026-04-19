package sk.stuba.fiit.core.engine;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.world.Level;

import java.util.Collections;
import java.util.List;

/**
 * Immutable context object passed to {@link Updatable#update(UpdateContext)}.
 *
 * <p>Different object types (character, projectile, enemy) need different
 * combinations of input data during their update. Instead of multiple overloaded
 * methods or a deteriorating interface contract, all data is bundled here –
 * each implementor takes what it needs and ignores the rest.
 *
 * <p>No class receiving this object needs to import {@code GameManager}.
 */
public final class UpdateContext {

    /** Time elapsed since the last frame in seconds. */
    public final float deltaTime;

    /**
     * Collision rectangles from the map (platforms, walls).
     * Never {@code null}; an empty list is used when no map is loaded.
     */
    public final List<Rectangle> platforms;

    /**
     * The current level. May be {@code null} before the level is loaded
     * (e.g., on the inventory screen). Each object performs its own null-check.
     */
    public final Level level;

    /**
     * The active player character. May be {@code null} if the party has been
     * defeated or not yet initialised. AI and projectiles null-check before use.
     */
    public final PlayerCharacter player;

    // -------------------------------------------------------------------------
    //  Konštruktory
    // -------------------------------------------------------------------------

    public UpdateContext(float deltaTime,
                         List<Rectangle> platforms,
                         Level level,
                         PlayerCharacter player) {
        this.deltaTime = deltaTime;
        this.platforms = (platforms != null) ? platforms : Collections.emptyList();
        this.level     = level;
        this.player    = player;
    }

    /** Skrátený konštruktor – len čas a platformy (napr. pre projektily). */
    public UpdateContext(float deltaTime, List<Rectangle> platforms) {
        this(deltaTime, platforms, null, null);
    }

    /** Minimálny konštruktor – len čas (napr. pre UI elementy). */
    public UpdateContext(float deltaTime) {
        this(deltaTime, Collections.emptyList(), null, null);
    }
}
