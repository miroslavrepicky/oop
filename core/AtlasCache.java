package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight Factory pre TextureAtlas objekty.
 *
 * Problém: AnimationManager pôvodne volal new TextureAtlas(path) pri každej
 * inštancii – dva EnemyKnight znamenali dva identické atlasy v GPU pamäti.
 *
 * Riešenie: AtlasCache drží jednu inštanciu atlasu per cesta a zdieľa ju
 * medzi všetkými AnimationManager klientmi. Intrinsic state (pixely atlasu)
 * je zdieľaný; extrinsic state (stateTime, currentAnimation) zostáva
 * v AnimationManager.
 *
 * Životný cyklus: dispose() zavolá GameManager.resetGame() alebo
 * ApplicationAdapter.dispose() – nie AnimationManager.
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
     * Vráti zdieľaný atlas pre danú cestu.
     * Ak atlas ešte nie je načítaný, načíta ho práve raz.
     *
     * @param path relatívna cesta k .atlas súboru (napr. "atlas/knight/knight.atlas")
     * @return zdieľaná inštancia TextureAtlas
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
     * Uvoľní všetky atlasy z GPU pamäte.
     * Volať len pri ukončení hry alebo pri plnom reštarte (resetGame).
     * NIKDY nevolať počas levelu – zrušilo by to textúry živých postáv.
     */
    public void dispose() {
        cache.values().forEach(TextureAtlas::dispose);
        cache.clear();
    }

    /**
     * Vráti počet unikátnych atlasov v cache (užitočné pre debug/logging).
     */
    public int size() {
        return cache.size();
    }
}
