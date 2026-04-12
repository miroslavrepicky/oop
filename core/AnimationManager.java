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

    /**
     * Kresli aktualny frame v SKUTOCNEJ velkosti tohto konkretneho framu
     * (packedWidth / packedHeight), ukotveny na spodny stred hitboxu.
     *
     * Kazdy frame ma svoju prirodzenu velkost -> animacia sa neroztahuje
     * do pevného obdlznika. Vlniaci sa plast, pohyb hore-dole atd. vyzeraju
     * spravne, pretoze sprite jednoducho "vycnieva" mimo hitbox podla potreby.
     *
     * @param x       lavý okraj hitboxu / pozicie postavy vo svete
     * @param y       spodny okraj hitboxu / pozicie postavy vo svete
     * @param hitboxW sírka hitboxu — pouzita na horizontalne centrovanie spritu
     * @param flipX   ci otocit sprite horizontalne (postava ide dolava)
     */
    public void renderActualSize(SpriteBatch batch, float x, float y,
                                 float hitboxW, boolean flipX) {
        if (currentAnimation == null) return;
        Animation<TextureAtlas.AtlasRegion> anim = animations.get(currentAnimation);
        if (anim == null) return;

        boolean looping = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        TextureAtlas.AtlasRegion frame = anim.getKeyFrame(stateTime, looping);

        float frameW = frame.packedWidth;
        float frameH = frame.packedHeight;

        float drawX;
        if (!flipX) {
            // Postava ide doprava: ukotvenie na spodny pravy roh hitboxu,
            // sprite vycnieva dolava
            drawX = x + hitboxW - frameW;
        } else {
            // Postava ide dolava: ukotvenie na spodny lavy roh hitboxu,
            // sprite vycnieva doprava
            drawX = x;
        }

        batch.draw(
            frame,
            flipX ? drawX + frameW : drawX, y,
            flipX ? -frameW : frameW,
            frameH
        );
    }

    /**
     * Vracia maximalnu velkost (sirka x vyska) zo VSETKYCH framov danej animacie.
     * Pouziva packedWidth/packedHeight – realne rozmery sprite-u v atla
     *
     * Dovod: niektore animacie maju framy roznych rozmerov (napr. vlniaci sa
     * plast Knighta). Pouzitim maxima dostaneme stabilny bounding box pre
     * celu animaciu bez "skakania".
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

//    public Vector2D getAnimationSize(String name){
//        Animation<TextureAtlas.AtlasRegion> anim = animations.get(name);
//
//        //TODO real animation size
//        //return new Vector2D(anim.getKeyFrame(stateTime).originalWidth, anim.getKeyFrame(0).originalHeight);
//        return new Vector2D(anim.getKeyFrame(stateTime).packedWidth, anim.getKeyFrame(0).packedHeight);
//    }

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

    /** Vracia nazov aktualne prehravane animacie (potrebne pre GameRenderer). */
    public String getCurrentAnimation() {
        return currentAnimation;
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public void dispose() {
        atlas.dispose();
    }
}
