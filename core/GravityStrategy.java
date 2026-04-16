package sk.stuba.fiit.core;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.characters.Character;

import java.util.List;

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
