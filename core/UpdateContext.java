package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.world.Level;

import java.util.Collections;
import java.util.List;

/**
 * Kontextový objekt predávaný do {@link Updatable#update(UpdateContext)}.
 *
 * Dôvod existencie: jednotlivé typy objektov (postava, projektil, nepriateľ)
 * potrebujú pri update rôzne kombinácie vstupných dát. Namiesto viacerých
 * preťažených metód alebo rozpadajúceho sa kontraktu interfacu zabalíme
 * všetko do jedného objektu – každý si zoberie čo potrebuje a zvyšok ignoruje.
 *
 * Žiadna z tried, ktoré tento objekt dostanú, nemusí importovať GameManager.
 */
public final class UpdateContext {

    /** Čas od posledného snímka v sekundách. */
    public final float deltaTime;

    /**
     * Kolízne obdĺžniky z mapy – platformy, steny.
     * Nikdy nie {@code null}; ak mapa nie je načítaná, je to prázdny zoznam.
     */
    public final List<Rectangle> platforms;

    /**
     * Aktuálny level. Môže byť {@code null} pred načítaním levelu
     * (napr. na obrazovke inventára). Každý objekt si sám rozhodne,
     * či level potrebuje, a prípadne null-check vykoná.
     */
    public final Level level;

    /**
     * Aktívna hráčska postava. Môže byť {@code null} ak party bola porazená
     * alebo ešte nebola inicializovaná. AI a projektily si null-check vykonajú.
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
