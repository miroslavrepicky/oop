package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight factory for {@link TextureAtlas} instances.
 *
 * <p>Problem: {@code AnimationManager} originally called {@code new TextureAtlas(path)}
 * per instance – two {@code EnemyKnight} characters meant two identical atlases in
 * GPU memory.
 *
 * <p>Solution: {@code AtlasCache} keeps one atlas instance per path and shares it
 * across all {@code AnimationManager} clients. The intrinsic state (pixel data) is
 * shared; the extrinsic state ({@code stateTime}, {@code currentAnimation}) remains
 * in each {@code AnimationManager}.
 *
 * <p>Lifecycle: {@link #dispose()} is called by {@code GameManager.resetGame()} or
 * {@code ApplicationAdapter.dispose()} – never by {@code AnimationManager}.
 */
public final class AtlasCache {

    private static AtlasCache instance;

    private final Map<String, TextureAtlas> cache = new HashMap<>();

    private static final Logger log = GameLogger.get(AtlasCache.class);

    private AtlasCache() {}

    public static AtlasCache getInstance() {
        if (instance == null) {
            instance = new AtlasCache();
        }
        return instance;
    }

    /**
     * Returns the shared atlas for the given path, loading it from disk on first access.
     *
     * @param path relative path to the {@code .atlas} file
     * @return shared {@link TextureAtlas} instance
     */
    public TextureAtlas get(String path) {
        if (cache.containsKey(path)) {
            // Guard – cache.containsKey sa volá často
            if (log.isDebugEnabled()) {
                log.debug("Atlas cache hit: path={}", path);
            }
            return cache.get(path);
        }
        log.info("Atlas cache miss – loading from disk: path={}", path);
        return cache.computeIfAbsent(path,
            p -> new TextureAtlas(Gdx.files.internal(p)));
    }

    /**
     * Releases all atlases from GPU memory and clears the cache.
     * Must NOT be called during an active level – it would invalidate textures
     * used by live characters.
     */
    public void dispose() {
        cache.values().forEach(TextureAtlas::dispose);
        cache.clear();
    }

    /**
     * Returns the number of unique atlases currently held in the cache.
     * Useful for debug logging and diagnostics.
     */
    public int size() {
        return cache.size();
    }
}
