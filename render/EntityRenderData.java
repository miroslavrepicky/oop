package sk.stuba.fiit.render;

import sk.stuba.fiit.core.AnimationManager;

/**
 * Immutable data transfer object describing a single visual entity for the renderer.
 *
 * <p>Motivation: {@code GameRenderer} previously imported {@code EnemyCharacter},
 * {@code PlayerCharacter}, {@code Duck}, {@code Item} and {@code Projectile} just
 * to call {@code .getPosition().getX()}, {@code .getAnimationManager()} and
 * {@code .isFacingRight()}. This constitutes a violation of MVC – the View depended
 * on the entire Model.
 *
 * <p>After refactoring: the Controller ({@code PlayingState}) assembles an
 * {@code EntityRenderData} from each live object. {@code GameRenderer} receives
 * only this DTO – it does not import a single model class.
 *
 * <p>Contents:
 * <ul>
 *   <li>Position and hitbox dimensions (float primitives)</li>
 *   <li>{@link AnimationManager} – the only "model" dependency, but it is a purely
 *       visual class (no game logic), so the View is permitted to know it</li>
 *   <li>{@code flipX}, {@code isAttacking} – simple booleans</li>
 *   <li>{@code renderType} – enum telling the renderer how to draw (actualSize vs fixedRect)</li>
 *   <li>For projectiles: {@code renderWidth}/{@code renderHeight}/{@code offsetX}/{@code offsetY}</li>
 * </ul>
 *
 * <p>The class is immutable (all fields are {@code final}). The Builder pattern
 * makes construction convenient – not all fields are relevant for every type.
 */
public final class EntityRenderData {

    /**
     * Rendering mode for this entity.
     */
    public enum RenderType {
        /**
         * Drawn at the actual frame size, anchored to the bottom-centre of the
         * hitbox (characters, ducks).
         */
        ACTUAL_SIZE,
        /**
         * Drawn stretched to fill a fixed rectangle (projectiles, items).
         */
        FIXED_RECT
    }

    public final float          x;
    public final float          y;
    public final float          hitboxWidth;
    public final float          hitboxHeight;
    public final AnimationManager animationManager;
    public final boolean        flipX;
    public final boolean        isAttacking;
    public final RenderType     renderType;

    public final float          renderWidth;
    public final float          renderHeight;
    public final float          renderOffsetX;
    public final float          renderOffsetY;

    /** HP and armour bars (for enemies, 0 = do not display). */
    public final int  hp;
    public final int  maxHp;
    public final int  armor;
    public final int  maxArmor;

    /** RGB tint multipliers applied to the sprite (1, 1, 1 = no tint). */
    public final float tintR;
    public final float tintG;
    public final float tintB;

    private EntityRenderData(Builder b) {
        this.x               = b.x;
        this.y               = b.y;
        this.hitboxWidth     = b.hitboxWidth;
        this.hitboxHeight    = b.hitboxHeight;
        this.animationManager = b.animationManager;
        this.flipX           = b.flipX;
        this.isAttacking     = b.isAttacking;
        this.renderType      = b.renderType;
        this.renderWidth     = b.renderWidth;
        this.renderHeight    = b.renderHeight;
        this.renderOffsetX   = b.renderOffsetX;
        this.renderOffsetY   = b.renderOffsetY;
        this.hp              = b.hp;
        this.maxHp           = b.maxHp;
        this.armor           = b.armor;
        this.maxArmor        = b.maxArmor;
        this.tintR = b.tintR;
        this.tintG = b.tintG;
        this.tintB = b.tintB;
    }

    /**
     * Creates a new {@link Builder} with the mandatory position and animation manager.
     *
     * @param x  entity X position in world coordinates
     * @param y  entity Y position in world coordinates
     * @param am the entity's animation manager; may be {@code null} (fallback shape is drawn)
     * @return a new builder instance
     */
    public static Builder builder(float x, float y, AnimationManager am) {

        return new Builder(x, y, am);
    }

    /**
     * Fluent builder for {@link EntityRenderData}.
     * All optional fields default to sensible values (64×64 hitbox, no flip, no tint, etc.).
     */
    public static final class Builder {
        private final float x, y;
        private final AnimationManager animationManager;
        private float   hitboxWidth   = 64f;
        private float   hitboxHeight  = 64f;
        private boolean flipX         = false;
        private boolean isAttacking   = false;
        private RenderType renderType = RenderType.ACTUAL_SIZE;
        private float   renderWidth   = 64f;
        private float   renderHeight  = 64f;
        private float   renderOffsetX = 0f;
        private float   renderOffsetY = 0f;
        private int     hp      = 0;
        private int     maxHp   = 0;
        private int     armor   = 0;
        private int     maxArmor = 0;
        private float tintR = 1f;
        private float tintG = 1f;
        private float tintB = 1f;


        private Builder(float x, float y, AnimationManager am) {
            this.x = x;
            this.y = y;
            this.animationManager = am;
        }

        /** Sets the hitbox dimensions used for debug rendering and bar placement. */
        public Builder hitbox(float w, float h)  { hitboxWidth = w; hitboxHeight = h; return this; }
        /** Sets whether the sprite should be flipped horizontally. */
        public Builder flipX(boolean v)           { flipX = v; return this; }
        /** Marks the entity as currently performing an attack animation. */
        public Builder attacking(boolean v)       { isAttacking = v; return this; }
        /** Sets the render type ({@link RenderType#ACTUAL_SIZE} or {@link RenderType#FIXED_RECT}). */
        public Builder renderType(RenderType t)   { renderType = t; return this; }
        /** Sets the fixed render dimensions (used for {@link RenderType#FIXED_RECT}). */
        public Builder renderSize(float w, float h){ renderWidth = w; renderHeight = h; return this; }
        /** Sets the render offset relative to the entity's world position. */
        public Builder renderOffset(float ox, float oy){ renderOffsetX = ox; renderOffsetY = oy; return this; }
        /** Sets the HP and armour bar values for enemy health display. */
        public Builder bars(int hp, int maxHp, int armor, int maxArmor) {
            this.hp = hp; this.maxHp = maxHp;
            this.armor = armor; this.maxArmor = maxArmor;
            return this;
        }
        /** Sets the RGB tint applied to the sprite during rendering. */
        public Builder tint(float r, float g, float b) {
            tintR = r; tintG = g; tintB = b;
            return this;
        }

        /** Builds and returns the immutable {@link EntityRenderData} instance. */
        public EntityRenderData build() { return new EntityRenderData(this); }
    }
}
