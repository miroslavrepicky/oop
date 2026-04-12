package sk.stuba.fiit.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import sk.stuba.fiit.util.Vector2D;

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

    public void addAnimation(String name, String regionName, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            System.out.println("Region nenajdeny: " + regionName);
            return;
        }
        Animation<TextureAtlas.AtlasRegion> animation =
            new Animation<>(frameDuration, regions, playMode);
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

        boolean looping = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        TextureAtlas.AtlasRegion frame = anim.getKeyFrame(stateTime, looping);
        // TODO size of animation
//        Vector2D vv = getAnimationSize(currentAnimation);
//        height = vv.getY();
//        width = vv.getX();

        batch.draw(
            frame,
            flipX ? x + width : x, y,
            flipX ? -width : width,
            height
        );
    }

    public Vector2D getAnimationSize(String name){
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);

        //TODO real animation size
        //return new Vector2D(anim.getKeyFrame(stateTime).originalWidth, anim.getKeyFrame(0).originalHeight);
        return new Vector2D(anim.getKeyFrame(stateTime).packedWidth, anim.getKeyFrame(0).packedHeight);
    }

    public float getAnimationDuration(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return 0f;
        return anim.getAnimationDuration();
    }

    public int getFrameCount(String name) {
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
        if (anim == null) return 1;
        return anim.getKeyFrames().length;
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public void dispose() {
        atlas.dispose();
    }
}
