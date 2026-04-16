package sk.stuba.fiit.core.engine;

import com.badlogic.gdx.math.Rectangle;
import sk.stuba.fiit.physics.GravityStrategy;
import sk.stuba.fiit.util.Vector2D;

/**
 * Kontrakt pre objekty, na ktoré môže pôsobiť {@link GravityStrategy}.
 *
 * Dôvod existencie: {@link GravityStrategy} pôvodne závisela na {@code Character},
 * čo znemožňovalo gravitáciu pre projektily alebo iné objekty bez zbytočnej
 * dedičnosti. Tento interface exponuje len to, čo gravitácia skutočne potrebuje:
 * pozíciu, rýchlosť, hitbox a príznak onGround.
 *
 * Implementujú: {@code Character} a voliteľne {@code Projectile} alebo iné objekty.
 */
public interface Physicable {

    // --- pozícia ---
    Vector2D getPosition();
    void     setPosition(Vector2D position);

    // --- rýchlosť ---
    float getVelocityY();
    void  setVelocityY(float vy);

    // --- hitbox (gravitácia ho potrebuje na kolízie) ---
    Rectangle getHitbox();
    void      updateHitbox();

    // --- stav na zemi ---
    boolean isOnGround();
    void    setOnGround(boolean onGround);
}
