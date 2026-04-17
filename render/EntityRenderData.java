package sk.stuba.fiit.render;

import sk.stuba.fiit.core.AnimationManager;

/**
 * DTO popisujúci jeden vizuálny objekt pre renderer.
 *
 * Dôvod existencie: GameRenderer pôvodne importoval EnemyCharacter,
 * PlayerCharacter, Duck, Item a Projectile len preto, aby zavolal
 * .getPosition().getX(), .getAnimationManager() a .isFacingRight().
 * To je porušenie MVC – View závisí na celom Modeli.
 *
 * Po refaktore: Controller (PlayingState) zostaví EntityRenderData
 * z každého živého objektu. GameRenderer dostane len tento DTO –
 * neimportuje ani jednu model triedu.
 *
 * Čo DTO nesie:
 *  - pozícia a rozmery hitboxu (float primitívy)
 *  - AnimationManager – jedina "model" závislosť, ale je to čisto
 *    vizuálna trieda (žiadna herná logika), takže View ju smie poznať
 *  - flipX, isAttacking – jednoduché booleany
 *  - renderType – enum, aby renderer vedel ako kresliť (actualSize vs fixedRect)
 *  - pre projektily: renderWidth/renderHeight/offsetX/offsetY
 *
 * Trieda je immutable (všetky polia final). Builder pattern
 * uľahčuje konštrukciu – nie všetky polia sú relevantné pre každý typ.
 */
public final class EntityRenderData {

    public enum RenderType {
        /** Kreslí sa v skutočnej veľkosti framu ukotvený na spodný stred hitboxu (postavy, kačky). */
        ACTUAL_SIZE,
        /** Kreslí sa do pevne zadaného obdĺžnika (projektily, itemy). */
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

    /** Len pre FIXED_RECT – šírka vykresleného sprite-u. */
    public final float          renderWidth;
    /** Len pre FIXED_RECT – výška vykresleného sprite-u. */
    public final float          renderHeight;
    /** Horizontálny offset od pozície (napr. vajce pri výbuchu). */
    public final float          renderOffsetX;
    /** Vertikálny offset od pozície. */
    public final float          renderOffsetY;

    // HP/Armor bary (pre nepriateľov, 0 = nezobrazovať)
    public final int  hp;
    public final int  maxHp;
    public final int  armor;
    public final int  maxArmor;

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
    }

    public static Builder builder(float x, float y, AnimationManager am) {
        return new Builder(x, y, am);
    }

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

        private Builder(float x, float y, AnimationManager am) {
            this.x = x;
            this.y = y;
            this.animationManager = am;
        }

        public Builder hitbox(float w, float h)  { hitboxWidth = w; hitboxHeight = h; return this; }
        public Builder flipX(boolean v)           { flipX = v; return this; }
        public Builder attacking(boolean v)       { isAttacking = v; return this; }
        public Builder renderType(RenderType t)   { renderType = t; return this; }
        public Builder renderSize(float w, float h){ renderWidth = w; renderHeight = h; return this; }
        public Builder renderOffset(float ox, float oy){ renderOffsetX = ox; renderOffsetY = oy; return this; }
        public Builder bars(int hp, int maxHp, int armor, int maxArmor) {
            this.hp = hp; this.maxHp = maxHp;
            this.armor = armor; this.maxArmor = maxArmor;
            return this;
        }

        public EntityRenderData build() { return new EntityRenderData(this); }
    }
}
