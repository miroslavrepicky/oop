package sk.stuba.fiit.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sk.stuba.fiit.items.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stara sa o vykreslovanie ikon itemov na zemi.
 * Textury cachuje – kazda cesta sa nacita iba raz.
 */
public class ItemIconRenderer {
    private static final float ICON_SIZE = 32f;

    private final Map<String, Texture> cache = new HashMap<>();

    public void render(SpriteBatch batch, List<Item> items) {
        for (Item item : items) {
            String path = item.getIconPath();
            if (path == null) continue;

            Texture texture = cache.computeIfAbsent(path, Texture::new);
            batch.draw(texture,
                item.getPosition().getX(),
                item.getPosition().getY(),
                ICON_SIZE, ICON_SIZE);
        }
    }

    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
