package sk.stuba.fiit.render;

import sk.stuba.fiit.core.AnimationManager;

/**
 * Kontrakt pre objekty, ktoré vedia samy opísať ako sa majú nakresliť.
 *
 * Dôvod existencie: GameRenderer obsahoval sériu instanceof checkov
 * (MagicSpell, Arrow, EggProjectile...) a pre každý typ nastavoval
 * iné rozmery a offsety. Každý nový projektil = zmena v rendereri.
 *
 * Po refaktore: každý projektil implementuje Renderable a sám vie
 * svoje vizuálne parametre. GameRenderer robí len:
 *   AnimationRenderer.render(batch, r.getAnimationManager(), ...)
 * bez akejkoľvek znalosti konkrétneho typu.
 *
 * Implementujú: MagicSpell, Arrow, EggProjectile, TurdflyProjectile
 * Volá: GameRenderer (projektily), prípadne aj EnemyCharacter/Duck ak treba
 */
public interface Renderable {

    /** AnimationManager s aktuálnym framom. Null = objekt sa nekreslí. */
    AnimationManager getAnimationManager();

    /** True = sprite sa zrkadlovo otočí horizontálne (objekt ide doľava). */
    boolean isFlippedX();

    /** Šírka vykresleného sprite-u v herných súradniciach. */
    float getRenderWidth();

    /** Výška vykresleného sprite-u v herných súradniciach. */
    float getRenderHeight();

    /**
     * Horizontálny offset od pozície objektu.
     * Väčšina objektov vracia 0f; EggProjectile vracia -16f pri výbuchu.
     */
    default float getRenderOffsetX() { return 0f; }

    /**
     * Vertikálny offset od pozície objektu.
     * Väčšina objektov vracia 0f; EggProjectile vracia -16f pri výbuchu.
     */
    default float getRenderOffsetY() { return 0f; }
}
