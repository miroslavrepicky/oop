package sk.stuba.fiit.projectiles;

/**
 * Určuje kto vystrelil projektil.
 * Používa sa v CollisionManager namiesto {@code instanceof EnemyCharacter}
 * na rozlíšenie hráčskych a nepriateľských projektilov.
 */
public enum ProjectileOwner {
    PLAYER,
    ENEMY
}
