package sk.stuba.fiit.projectiles;

/**
 * Marker interface pre projektily s plosnym poskodenim.
 * Kazdy projektil ktory ho implementuje dostane AOE spracovanie
 * v triggerImpact() bez nutnosti instanceof checkov na konkretnu triedu.
 */
public interface AoeProjectile {
    float getAoeRadius();
    int   getDamage();
}
