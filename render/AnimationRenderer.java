package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import sk.stuba.fiit.core.AnimationManager;

/**
 * Stateless utility for drawing animated sprites to the screen.
 *
 * <p>Sole responsibility is View rendering:
 * <ol>
 *   <li>Retrieves the current frame from an {@link AnimationManager}.</li>
 *   <li>Draws it at the correct position via {@link SpriteBatch}.</li>
 * </ol>
 *
 * <p>No game logic or animation state lives here –
 * that is exclusively the responsibility of {@link AnimationManager}.
 *
 * <p>All methods are static: the renderer has no instance state,
 * preventing accidental state sharing between objects.
 */
public final class AnimationRenderer {

    private AnimationRenderer() {
        // utility class - no instances
    }

    /**
     * Draws the current animation frame stretched to fill the given rectangle.
     * Suitable for projectiles and objects with a fixed visual size.
     *
     * @param batch  active {@link SpriteBatch} (must be between {@code begin()}/{@code end()})
     * @param am     the object's {@link AnimationManager}
     * @param x      left edge in world coordinates
     * @param y      bottom edge in world coordinates
     * @param width  rendered sprite width
     * @param height rendered sprite height
     * @param flipX  {@code true} to mirror horizontally (character moving left)
     */
    public static void render(SpriteBatch batch, AnimationManager am,
                              float x, float y, float width, float height,
                              boolean flipX) {
        if (am == null) return;
        TextureAtlas.AtlasRegion frame = am.getCurrentFrame();
        if (frame == null) return;

        batch.draw(
            frame,
            flipX ? x + width : x, y,
            flipX ? -width : width,
            height
        );
    }

    /**
     * Draws the current animation frame at its native packed size, optionally
     * anchoring the sprite to the opposite side of the hitbox.
     *
     * <p>Characters are normally anchored so that the sprite's right edge aligns
     * with the right edge of the hitbox (facing right) or the left edge aligns
     * with the left edge (facing left). During an attack animation the sprite
     * "reaches forward" beyond the hitbox, so {@code anchorOpposite} reverses
     * the anchor point to keep the body part of the sprite inside the hitbox
     * while the weapon extends outward.
     *
     * @param batch          active {@link SpriteBatch}
     * @param am             the object's {@link AnimationManager}
     * @param x              hitbox left edge in world coordinates
     * @param y              hitbox bottom edge in world coordinates
     * @param hitboxW        width of the character's hitbox, used to compute alignment
     * @param flipX          {@code true} to mirror the sprite horizontally
     * @param anchorOpposite when {@code true}, anchors the sprite to the side of the hitbox
     *                       opposite to the character's facing direction; use {@code true}
     *                       during attack animations so the weapon extends beyond the hitbox
     */
    public static void renderActualSize(SpriteBatch batch, AnimationManager am,
                                        float x, float y, float hitboxW,
                                        boolean flipX, boolean anchorOpposite) {
        if (am == null) return;
        TextureAtlas.AtlasRegion frame = am.getCurrentFrame();
        if (frame == null) return;

        float frameW = frame.packedWidth;
        float frameH = frame.packedHeight;

        float drawX;
        if (!flipX) {
            drawX = anchorOpposite
                ? x
                : x + hitboxW - frameW;
        } else {
            drawX = anchorOpposite
                ? x + hitboxW - frameW
                : x;
        }

        batch.draw(
            frame,
            flipX ? drawX + frameW : drawX, y,
            flipX ? -frameW : frameW,
            frameH
        );
    }
}
