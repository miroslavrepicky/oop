package sk.stuba.fiit.render;

import sk.stuba.fiit.core.AnimationManager;

/**
 * Contract for objects that can describe their own rendering parameters.
 *
 * <p>Motivation: {@code GameRenderer} previously contained a chain of
 * {@code instanceof} checks (MagicSpell, Arrow, EggProjectile…) and set
 * different dimensions and offsets for each type. Every new projectile type
 * required a change to the renderer.
 *
 * <p>After refactoring: each projectile implements {@code Renderable} and
 * knows its own visual parameters. {@code GameRenderer} calls only:
 * <pre>
 *   AnimationRenderer.render(batch, r.getAnimationManager(), ...)
 * </pre>
 * with no knowledge of the concrete type.
 *
 * <p>Implemented by: {@code MagicSpell}, {@code Arrow}, {@code EggProjectile},
 * {@code TurdflyProjectile}.
 */
public interface Renderable {

    /** @return the animation manager holding the current frame; {@code null} = not drawn */
    AnimationManager getAnimationManager();

    /** @return {@code true} if the sprite should be flipped horizontally */
    boolean isFlippedX();

    /** @return rendered sprite width in world units */
    float getRenderWidth();
    /** @return rendered sprite height in world units */
    float getRenderHeight();

    /**
     * @return horizontal offset from the object's position (default {@code 0f});
     *         {@code EggProjectile} returns {@code -16f} during the blast phase
     */
    default float getRenderOffsetX() { return 0f; }

    /**
     * @return vertical offset from the object's position (default {@code 0f})
     */
    default float getRenderOffsetY() { return 0f; }
}
