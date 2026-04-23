package sk.stuba.fiit.characters;

import com.badlogic.gdx.math.Rectangle;
import org.slf4j.Logger;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameLogger;
import sk.stuba.fiit.physics.FloatingGravity;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.items.EggProjectileSpawner;
import sk.stuba.fiit.items.FriendlyDuck;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.util.Vector2D;

import java.util.List;
import java.util.Random;

/**
 * Non-combat ambient character that walks back and forth and can drop items when killed.
 *
 * <p>Movement pattern: alternates between walking ({@value #WALK_DURATION} s) and
 * idling ({@value #IDLE_DURATION} s), reversing direction each cycle.
 * Uses {@link sk.stuba.fiit.physics.FloatingGravity} for a gentle hover effect.
 *
 * <p>Drop on death: {@link #onKilled()} randomly returns either a
 * {@link sk.stuba.fiit.items.FriendlyDuck} (50 %) or an
 * {@link sk.stuba.fiit.items.EggProjectileSpawner} (50 %).
 */
public class Duck extends Character {
    private static final Logger log = GameLogger.get(Duck.class);
    private static final int   DUCK_HP     = 20;

    private float walkTimer  = 0f;
    private float idleTimer  = 0f;
    private boolean walking  = false;
    private float walkDir    = 1f;

    private static final float WALK_DURATION = 2.0f;
    private static final float IDLE_DURATION = 1.5f;
    private static final float WALK_SPEED    = 40f;

    private AnimationManager animationManager;

    public Duck(Vector2D position) {
        super("Duck", DUCK_HP, 0, 1.0f, position);
        this.gravityStrategy = new FloatingGravity();
        initAnimations();
    }

    private void initAnimations() {
        animationManager = new AnimationManager("atlas/duck/duck.atlas");
        animationManager.addAnimation("idle", "IDLE/IDLE", 0.4f);
        animationManager.addAnimation("walk", "WALK/WALK", 0.15f);
    }

    @Override
    public AnimationManager getAnimationManager() { return animationManager; }

    @Override
    public void move(Vector2D direction) {
        position = position.add(direction);
        updateHitbox();
    }

    /**
     * Updates the duck: applies gravity and advances the walk/idle cycle.
     *
     * @param ctx frame context; {@code deltaTime} and {@code platforms} are used
     */
    public void update(UpdateContext ctx) {
        float deltaTime = ctx.deltaTime;
        List<Rectangle> platforms = ctx.platforms;
        applyGravity(deltaTime, platforms);

        if (walking) {
            walkTimer -= deltaTime;
            move(new Vector2D(walkDir * WALK_SPEED * deltaTime, 0));
            animationManager.play("walk");
            setHitboxSize(animationManager.getAnimationSize("walk"));
            animationManager.update(deltaTime);
            if (walkTimer <= 0f) {
                walking   = false;
                idleTimer = IDLE_DURATION;
            }
            setFacingRight(walkDir > 0);
        } else {
            idleTimer -= deltaTime;
            animationManager.play("idle");
            setHitboxSize(animationManager.getAnimationSize("idle"));
            animationManager.update(deltaTime);
            if (idleTimer <= 0f) {
                walking   = true;
                walkTimer = WALK_DURATION;
                walkDir   = -walkDir;
            }
        }
    }

    @Override
    public void onCollision(Object other) { }

    /**
     * Called by the collision system when the duck's HP reaches zero.
     * Randomly selects and returns a drop item.
     *
     * @return a {@link sk.stuba.fiit.items.FriendlyDuck} or
     *         an {@link sk.stuba.fiit.items.EggProjectileSpawner}
     */
    public Item onKilled() {
        Random random = new Random();
        Item drop;
        if (random.nextBoolean()) {
            drop = new FriendlyDuck(new Vector2D(position.getX(), position.getY()));
        } else {
            drop = new EggProjectileSpawner(new Vector2D(position.getX(), position.getY()));
        }
        log.info("Duck killed – drop: type={}, pos=({},{})",
            drop.getClass().getSimpleName(),
            String.format("%.1f", position.getX()),
            String.format("%.1f", position.getY()));
        return drop;
    }
}
