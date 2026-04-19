package sk.stuba.fiit.core;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import sk.stuba.fiit.core.exceptions.AssetLoadException;
import sk.stuba.fiit.util.Vector2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages animation state for a single character or game object.
 *
 * <p>Responsibilities (Model + Controller):
 * <ul>
 *   <li>Loads animations from a texture atlas (via {@link AtlasCache}).</li>
 *   <li>Tracks the current animation name and its elapsed time.</li>
 *   <li>Provides the current frame to {@link sk.stuba.fiit.render.AnimationRenderer}.</li>
 * </ul>
 *
 * <p>Rendering is exclusively the responsibility of {@link sk.stuba.fiit.render.AnimationRenderer}.
 * This class does not import {@code SpriteBatch} or any rendering API.
 *
 * <p>Atlas instances are shared via {@link AtlasCache} (Flyweight pattern) –
 * two {@code EnemyKnight} instances share one atlas in GPU memory.
 */
public class AnimationManager {
    private TextureAtlas atlas;
    private Map<String, Animation<TextureAtlas.AtlasRegion>> animations;
    private Map<String, Float> frameDurations;
    private String currentAnimation;
    private float stateTime;
    private static final Logger log = GameLogger.get(AnimationManager.class);

    /**
     * Creates a manager backed by the atlas at the given path.
     * The atlas is obtained from {@link AtlasCache} and shared with other managers
     * using the same path.
     *
     * @param atlasPath relative path to the {@code .atlas} file
     */
    public AnimationManager(String atlasPath) {
        atlas = AtlasCache.getInstance().get(atlasPath);
        animations = new HashMap<>();
        frameDurations = new HashMap<>();
        stateTime = 0f;
    }

    /**
     * Registers a looping animation.
     *
     * @param name           internal key used to reference this animation
     * @param regionName     region prefix in the texture atlas
     * @param frameDuration  duration of each frame in seconds
     */
    public void addAnimation(String name, String regionName, float frameDuration) {
        addAnimation(name, regionName, frameDuration, Animation.PlayMode.LOOP);
    }

    /**
     * Registers an animation with an explicit play mode.
     *
     * @param name           internal key
     * @param regionName     region prefix in the texture atlas
     * @param frameDuration  per-frame duration in seconds
     * @param playMode       e.g. {@code Animation.PlayMode.NORMAL} for one-shot animations
     */
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

    /**
     * Switches to the named animation, resetting the timer if the animation changed.
     * Has no effect if the requested animation is already playing.
     *
     * @param name the animation key to play
     * @throws AssetLoadException if no animation with the given name was registered
     */
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

    /**
     * Advances the animation timer. Must be called once per frame.
     *
     * @param deltaTime time elapsed since the last frame in seconds
     */
    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    // -------------------------------------------------------------------------
    //  Prístup k frameom – používa AnimationRenderer
    // -------------------------------------------------------------------------

    /**
     * Returns the current frame of the active animation, or {@code null} if none is active.
     * Used by {@link sk.stuba.fiit.render.AnimationRenderer} to draw the sprite.
     *
     * @return the current {@link TextureAtlas.AtlasRegion}, or {@code null}
     */
    public TextureAtlas.AtlasRegion getCurrentFrame() {
        if (currentAnimation == null) return null;
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(currentAnimation);
        if (anim == null) return null;
        boolean looping = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        return anim.getKeyFrame(stateTime, looping);
    }

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
