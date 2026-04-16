package sk.stuba.fiit.physics;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.core.engine.Physicable;

import java.util.List;

/**
 * Stratégia gravitácie pre ľubovoľný objekt implementujúci {@link Physicable}.
 *
 * Zmena oproti pôvodnému kódu: parameter je {@link Physicable} namiesto
 * {@code Character}. Tým sa gravitácia odviazala od hierarchie postáv –
 * je možné ju aplikovať aj na {@code Projectile} alebo iný objekt
 * bez akejkoľvek zmeny implementácií (NormalGravity, FloatingGravity, NoGravity).
 */
public interface GravityStrategy {

    /**
     * Aplikuje gravitáciu na fyzikálny objekt.
     *
     * @param body      objekt, na ktorý sa gravitácia aplikuje
     * @param deltaTime čas od posledného snímka
     * @param platforms kolízne obdĺžniky mapy; {@code null} alebo prázdny = žiadne platformy
     */
    void apply(Physicable body, float deltaTime, List<Rectangle> platforms);
}
