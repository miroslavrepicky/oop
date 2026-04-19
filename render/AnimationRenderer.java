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
        // utility trieda – žiadne inštancie
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
     * Draws the current frame at its ACTUAL packed size, anchored to the
     * bottom-centre of the hitbox.
     *
     * <p>Because each frame has its natural size, the animation does not stretch
     * into a fixed rectangle. A billowing cloak or up-and-down motion looks
     * correct because the sprite simply extends beyond the hitbox as needed.
     *
     * @param batch   active {@link SpriteBatch}
     * @param am      the object's {@link AnimationManager}
     * @param x       hitbox left edge in world coordinates
     * @param y       hitbox bottom edge in world coordinates
     * @param hitboxW hitbox width used for horizontal centering of the sprite
     * @param flipX   {@code true} to mirror horizontally
     */
    public static void renderActualSize(SpriteBatch batch, AnimationManager am,
                                        float x, float y, float hitboxW,
                                        boolean flipX) {
        renderActualSize(batch, am, x, y, hitboxW, flipX, false);
    }

    /**
     * Rozšírená verzia s možnosťou otočenia strany kotvenia.
     *
     * @param anchorOpposite ak {@code true}, sprite sa ukotvuje na opačnú stranu hitboxu.
     *                       Používa sa pri útočných animáciách kde sprite „vyčnieva" dopredu.
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
