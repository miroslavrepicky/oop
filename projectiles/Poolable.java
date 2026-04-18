package sk.stuba.fiit.projectiles;

/**
 * Kontrakt pre projektily ktoré sú spravované cez {@link sk.stuba.fiit.core.ObjectPool}.
 *
 * Dôvod existencie: {@code Level.returnInactiveProjectilesToPool()} pôvodne
 * obsahoval sériu {@code instanceof} checkov (Arrow, MagicSpell, TurdflyProjectile).
 * Každý nový poolovaný projektil = zmena v {@code Level}.
 *
 * Po refaktore: každý poolovaný projektil implementuje {@code Poolable}
 * a sám vie ako sa vrátiť do správneho poolu. {@code Level} robí len:
 * <pre>
 *   if (p instanceof Poolable) ((Poolable) p).returnToPool();
 * </pre>
 *
 * Implementujú: {@link Arrow}, {@link MagicSpell}, {@link TurdflyProjectile}
 * Volá: {@code Level.returnInactiveProjectilesToPool()}
 */
public interface Poolable {

    /**
     * Vráti tento objekt späť do príslušného {@link sk.stuba.fiit.core.ObjectPool}.
     * Volá sa keď je projektil neaktívny, tesne pred jeho odstránením zo zoznamu.
     *
     * Implementácia zvyčajne vyzerá takto:
     * <pre>
     *   {@literal @}Override
     *   public void returnToPool() {
     *       ProjectilePool.getInstance().free(this);
     *   }
     * </pre>
     */
    void returnToPool();
}
