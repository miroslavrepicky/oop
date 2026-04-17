package sk.stuba.fiit.core.engine;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AIController;
import sk.stuba.fiit.util.Vector2D;

/**
 * Kontrakt ktorý musí implementovať každá postava ovládaná AI.
 *
 * {@link AIController} závisí výlučne od tohto interface –
 * nepozná {@code EnemyCharacter} ani žiadnu inú konkrétnu triedu.
 * Vďaka tomu je možné AI použiť aj pre iné typy postáv bez zmeny
 * logiky controllera.
 */
public interface AIControllable {

    // --- poloha a pohyb ---
    Vector2D getPosition();
    void     move(Vector2D direction);
    void     jump(float force);
    boolean  isOnGround();
    boolean  wasLastMoveBlocked();
    float    getSpeed();

    // --- orientácia ---
    void    setFacingRight(boolean right);
    boolean isFacingRight();

    // --- velocity (synchronizácia s fyzikou) ---
    void setVelocityX(float vx);

    // --- detekovanie hráča ---
    boolean detectPlayer(PlayerCharacter player);

    // --- útok ---
    void performAttack(PlayerCharacter target);

    /** Vráti true ak nepriateľ práve prehráva útočnú animáciu. */
    boolean isAttacking();
}
