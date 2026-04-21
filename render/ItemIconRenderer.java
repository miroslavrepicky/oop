package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kreslí ikony itemov na zemi.
 *
 * ZMENA: metóda renderDTOs() pracuje s RenderSnapshot.ItemRenderData
 * namiesto pôvodného Item. Renderer neimportuje nič z model balíka.
 */
public class ItemIconRenderer {
    private static final float ICON_SIZE = 32f;

    private final Map<String, Texture> cache = new HashMap<>();

    /**
     * Kreslí ikony zo zoznamu ItemRenderData DTO.
     * Volá sa z GameRenderer namiesto pôvodného render(batch, List<Item>).
     */
    public void renderDTOs(SpriteBatch batch, List<RenderSnapshot.ItemRenderData> items) {
        for (RenderSnapshot.ItemRenderData item : items) {
            if (item.iconPath == null) continue;
            Texture texture = cache.computeIfAbsent(item.iconPath, Texture::new);
            batch.draw(texture, item.x, item.y, ICON_SIZE, ICON_SIZE);
        }
    }

    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
