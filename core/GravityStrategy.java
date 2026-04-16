package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;

import java.util.List;

/**
 * Stratégia gravitácie pre {@link Character} a jeho podtriedy.
 *
 * Projektily gravitáciu nepoužívajú – ich pohyb je riadený
 * výlučne cez {@code direction * speed} v {@code Projectile.move()}.
 */
public interface GravityStrategy {
    /**
     * Aplikuje gravitáciu na postavu.
     *
     * @param character postava na ktorú sa aplikuje gravitácia
     * @param deltaTime čas od posledného framu
     * @param platforms zoznam kolíznych obdĺžnikov z mapy (môže byť null alebo prázdny)
     */
    void apply(Character character, float deltaTime, List<Rectangle> platforms);
}
