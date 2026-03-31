package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    private TextureAtlas atlas;
    private Map<String, Animation<TextureAtlas.AtlasRegion>> animations;
    private Map<String, Float> frameDurations;
    private String currentAnimation;
    private float stateTime;

    public AnimationManager(String atlasPath) {
        atlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        animations = new HashMap<>();
        frameDurations = new HashMap<>();
        stateTime = 0f;
    }

    public void addAnimation(String name, String regionName, float frameDuration) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            System.out.println("Region nenajdeny: " + regionName);
            return;
        }
        Animation<TextureAtlas.AtlasRegion> animation =
            new Animation<>(frameDuration, regions, Animation.PlayMode.LOOP);
        animations.put(name, animation);
        frameDurations.put(name, frameDuration);
    }

    public void play(String name) {
        if (!name.equals(currentAnimation)) {
            currentAnimation = name;
            stateTime = 0f;
        }
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public void render(SpriteBatch batch, float x, float y,
                       float width, float height, boolean flipX) {
        if (currentAnimation == null) return;
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(currentAnimation);
        if (anim == null) return;

        TextureAtlas.AtlasRegion frame = anim.getKeyFrame(stateTime, true);

        batch.draw(
            frame,
            flipX ? x + width : x, y,
            flipX ? -width : width,
            height
        );
    }

    public float getAnimationDuration(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return 0f;
        return anim.getAnimationDuration();
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public void dispose() {
        atlas.dispose();
    }
}
