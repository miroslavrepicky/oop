package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import sk.stuba.fiit.core.exceptions.AssetLoadException;
import sk.stuba.fiit.util.Vector2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Spravuje stav animácií pre jednu postavu/objekt.
 *
 * Zodpovednosť tejto triedy je výlučne MODEL a CONTROLLER:
 *  - načítanie animácií z atlasu
 *  - sledovanie aktuálnej animácie a jej časovača
 *  - poskytovanie aktuálneho framu volajúcemu
 *
 * Kreslenie na obrazovku je VÝLUČNE zodpovednosťou {@link sk.stuba.fiit.render.AnimationRenderer}.
 * Táto trieda neimportuje SpriteBatch ani žiadne rendering API.
 */
public class AnimationManager {
    private TextureAtlas atlas;
    private Map<String, Animation<TextureAtlas.AtlasRegion>> animations;
    private Map<String, Float> frameDurations;
    private String currentAnimation;
    private float stateTime;
    private static final Logger log = GameLogger.get(AnimationManager.class);

    public AnimationManager(String atlasPath) {
        atlas = AtlasCache.getInstance().get(atlasPath);
        animations = new HashMap<>();
        frameDurations = new HashMap<>();
        stateTime = 0f;
    }

    // -------------------------------------------------------------------------
    //  Registrácia animácií
    // -------------------------------------------------------------------------

    public void addAnimation(String name, String regionName, float frameDuration) {
        addAnimation(name, regionName, frameDuration, Animation.PlayMode.LOOP);
    }

    public void addAnimation(String name, String regionName, float frameDuration,
                             Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            log.warn("Atlas region not found – animation skipped: region={}, animation={}",
                regionName, name);
            return;
        }
        Animation<TextureAtlas.AtlasRegion> animation =
            new Animation<>(frameDuration, regions, playMode);
        animations.put(name, animation);
        frameDurations.put(name, frameDuration);
        if (log.isDebugEnabled()) {
            log.debug("Animation registered: name={}, region={}, frames={}, frameDuration={}, playMode={}",
                name, regionName, regions.size, frameDuration, playMode);
        }
    }

    // -------------------------------------------------------------------------
    //  Riadenie prehrávania
    // -------------------------------------------------------------------------

    /** Spustí animáciu (resetuje časovač ak sa animácia zmenila). */
    public void play(String name) {
        if (!name.equals(currentAnimation)) {
            if (!animations.containsKey(name)) {
                log.error("Attempted to play unknown animation: name={}", name);
                throw new AssetLoadException("animation:" + name);
            }
            if (log.isDebugEnabled()) {
                log.debug("Animation changed: from={}, to={}", currentAnimation, name);
            }
            currentAnimation = name;
            stateTime = 0f;
        }
    }

    /** Posúva časovač animácie. Volá sa každý frame. */
    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    // -------------------------------------------------------------------------
    //  Prístup k frameom – používa AnimationRenderer
    // -------------------------------------------------------------------------

    /**
     * Vráti aktuálny frame aktívnej animácie, alebo {@code null} ak žiadna nie je aktívna.
     * {@link sk.stuba.fiit.render.AnimationRenderer} tento frame nakreslí.
     */
    public TextureAtlas.AtlasRegion getCurrentFrame() {
        if (currentAnimation == null) return null;
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(currentAnimation);
        if (anim == null) return null;
        boolean looping = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        return anim.getKeyFrame(stateTime, looping);
    }

    /**
     * Vráti konkrétny frame zadanej animácie podľa časovača.
     * Používa sa keď renderer potrebuje frame inej animácie ako aktuálnej.
     */
    public TextureAtlas.AtlasRegion getFrame(String animationName, float time) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(animationName);
        if (anim == null) return null;
        boolean looping = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        return anim.getKeyFrame(time, looping);
    }

    // -------------------------------------------------------------------------
    //  Metadáta o animáciách – používajú Character, Attack, AnimationRenderer
    // -------------------------------------------------------------------------

    /**
     * Vráti veľkosť prvého framu danej animácie (packedWidth × packedHeight).
     * Používa sa pri inicializácii hitboxu postavy.
     */
    public Vector2D getFirstFrameSize(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return new Vector2D(64, 64);
        TextureAtlas.AtlasRegion frame = anim.getKeyFrames()[0];
        return new Vector2D(frame.packedWidth, frame.packedHeight);
    }

    /**
     * Vráti maximálnu veľkosť (šírka × výška) zo VŠETKÝCH framov danej animácie.
     * Používa packedWidth/packedHeight – reálne rozmery sprite-u v atlase.
     *
     * Dôvod: niektoré animácie majú framy rôznych rozmerov (napr. vlniaci sa
     * plášť Knighta). Použitím maxima dostaneme stabilný bounding box pre
     * celú animáciu bez „skákania".
     */
    public Vector2D getAnimationSize(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return new Vector2D(64, 64);

        float maxW = 0, maxH = 0;
        for (TextureAtlas.AtlasRegion region : anim.getKeyFrames()) {
            if (region.packedWidth  > maxW) maxW = region.packedWidth;
            if (region.packedHeight > maxH) maxH = region.packedHeight;
        }
        return new Vector2D(maxW, maxH);
    }

    /** Celková dĺžka animácie v sekundách. */
    public float getAnimationDuration(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return 0f;
        return anim.getAnimationDuration();
    }

    /** Počet framov danej animácie. */
    public int getFrameCount(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return 1;
        return anim.getKeyFrames().length;
    }

    /** Názov aktuálne prehrávanej animácie. */
    public String getCurrentAnimation() {
        return currentAnimation;
    }

    /** Aktuálny časovač animácie. Potrebný pre AnimationRenderer pri výpočte pozície. */
    public float getStateTime() {
        return stateTime;
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public void dispose() {
        animations.clear();
        frameDurations.clear();
    }
}
