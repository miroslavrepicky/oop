package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders item icons lying on the ground.
 *
 * <p>Operates exclusively on {@link RenderSnapshot.ItemRenderData} DTOs so that
 * the renderer has no dependency on any model package class.
 */
public class ItemIconRenderer {
    private static final float ICON_SIZE = 32f;

    private final Map<String, Texture> cache = new HashMap<>();

    /**
     * Renders icons from a list of {@link RenderSnapshot.ItemRenderData} DTOs.
     * Called by {@link GameRenderer} instead of the original {@code render(batch, List<Item>)}.
     *
     * @param batch active {@link SpriteBatch} (must be between {@code begin()}/{@code end()})
     * @param items list of item render descriptors to draw
     */
    public void renderDTOs(SpriteBatch batch, List<RenderSnapshot.ItemRenderData> items) {
        for (RenderSnapshot.ItemRenderData item : items) {
            if (item.iconPath == null) continue;
            Texture texture = cache.computeIfAbsent(item.iconPath, Texture::new);
            batch.draw(texture, item.x, item.y, ICON_SIZE, ICON_SIZE);
        }
    }

    /**
     * Disposes all cached textures and clears the cache.
     * Must be called when the renderer is no longer needed.
     */
    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
