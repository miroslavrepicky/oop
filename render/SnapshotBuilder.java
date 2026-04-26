package sk.stuba.fiit.render;

import sk.stuba.fiit.characters.Duck;
import sk.stuba.fiit.characters.EnemyCharacter;
import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.items.Item;
import sk.stuba.fiit.projectiles.Projectile;
import sk.stuba.fiit.world.Level;
import sk.stuba.fiit.world.MapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts live model objects into a {@link RenderSnapshot} DTO for the renderer.
 *
 * <h2>MVC role</h2>
 * <p>This is the Controller layer between the game model and the View
 * ({@link GameRenderer}). It is the single place where model classes are
 * translated into render DTOs, making it straightforward to test and extend.
 *
 * <h2>Why a separate class</h2>
 * <ul>
 *   <li>{@code PlayingState} already has enough responsibilities (state machine).</li>
 *   <li>The builder can be tested in isolation without a running game loop.</li>
 *   <li>{@code GameOverDelayState} needs the same conversion logic – both states
 *       call {@code SnapshotBuilder.build()} instead of duplicating code.</li>
 * </ul>
 *
 * <p><b>Rule:</b> this class <em>may</em> import model classes.
 * {@link GameRenderer} must <em>not</em> import model classes.
 */
public class SnapshotBuilder {

    /**
     * Builds a complete render snapshot from the current level state.
     *
     * <p>All model objects ({@code PlayerCharacter}, {@code EnemyCharacter},
     * {@code Item}, {@code Projectile}, {@code Duck}) are converted to their
     * corresponding DTO types. The returned snapshot contains no model references
     * and is safe to hand to {@link GameRenderer}.
     *
     * @param player        the active player character; may be {@code null} when the
     *                      party is defeated
     * @param level         the current level; may be {@code null} before the level loads
     * @param inventory     the shared inventory; may be {@code null} in non-gameplay
     *                      contexts (e.g. in {@code GameOverDelayState})
     * @param debugHitboxes {@code true} if hitbox outlines should be drawn (F1 toggle)
     * @param nearbyItem    {@code true} if the pick-up hint should be shown in the HUD
     * @return an immutable {@link RenderSnapshot} ready for the renderer
     */
    public static RenderSnapshot build(PlayerCharacter player,
                                       Level level,
                                       Inventory inventory,
                                       boolean debugHitboxes,
                                       boolean nearbyItem) {
        EntityRenderData playerData = player != null ? buildPlayer(player) : null;

        List<EntityRenderData>              enemies     = new ArrayList<>();
        List<EntityRenderData>              ducks       = new ArrayList<>();
        List<RenderSnapshot.ItemRenderData> items       = new ArrayList<>();
        List<EntityRenderData>              projectiles = new ArrayList<>();
        RenderSnapshot.MapRenderData        mapData     = null;

        if (level != null) {
            for (EnemyCharacter e : level.getEnemies()) {
                enemies.add(buildEnemy(e));
            }
            for (Duck d : level.getDucks()) {
                if (d.isAlive()) ducks.add(buildDuck(d));
            }
            for (Item item : level.getItems()) {
                items.add(buildItem(item));
            }
            for (Projectile p : level.getProjectiles()) {
                if (p.isActive()) projectiles.add(buildProjectile(p));
            }

            MapManager mm = level.getMapManager();
            if (mm != null) {
                mapData = new RenderSnapshot.MapRenderData(
                    mm::render,
                    mm.getHitboxes()
                );
            }
        }

        RenderSnapshot.HUDSnapshot hud = buildHUD(inventory, nearbyItem);

        return new RenderSnapshot(
            playerData, enemies, ducks, items, projectiles,
            mapData, debugHitboxes, nearbyItem, hud
        );
    }

    // -------------------------------------------------------------------------
    //  HUD snapshot
    // -------------------------------------------------------------------------

    /**
     * Builds the {@link RenderSnapshot.HUDSnapshot} from the inventory.
     * Returns an empty snapshot when {@code inventory} is {@code null}.
     *
     * @param inventory  the shared inventory; may be {@code null}
     * @param nearbyItem {@code true} if the pick-up hint should be shown
     * @return a fully populated HUD snapshot
     */
    private static RenderSnapshot.HUDSnapshot buildHUD(Inventory inventory,
                                                       boolean   nearbyItem) {
        if (inventory == null) {
            return new RenderSnapshot.HUDSnapshot(
                new ArrayList<>(), 0, new ArrayList<>(), 0, 0, nearbyItem);
        }

        PlayerCharacter active = inventory.getActive();

        List<RenderSnapshot.HUDSnapshot.CharacterHUDData> chars = new ArrayList<>();
        for (PlayerCharacter c : inventory.getCharacters()) {
            chars.add(new RenderSnapshot.HUDSnapshot.CharacterHUDData(
                c.getName(),
                c.getHp(),
                c.getMaxHp(),
                c.getArmor(),
                c.getMaxArmor(),
                c == active,
                c.getCurrentMana(),
                c.getMaxMana(),
                c.getArrowCount(),
                c.getMaxArrows()
            ));
        }

        List<RenderSnapshot.HUDSnapshot.ItemSlotData> slots = new ArrayList<>();
        for (Item item : inventory.getItems()) {
            slots.add(new RenderSnapshot.HUDSnapshot.ItemSlotData(
                item.getIconPath(),
                item.getSlotsRequired()
            ));
        }

        return new RenderSnapshot.HUDSnapshot(
            chars,
            inventory.getSelectedSlot(),
            slots,
            inventory.getUsedSlots(),
            inventory.getTotalSlots(),
            nearbyItem
        );
    }

    // -------------------------------------------------------------------------
    //  Per-type conversion helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link PlayerCharacter} to an {@link EntityRenderData}.
     *
     * @param p the player to convert; must not be {@code null}
     * @return render descriptor for the player
     */
    private static EntityRenderData buildPlayer(PlayerCharacter p) {
        AnimationManager am = p.getAnimationManager();
        String currentAnim  = am != null ? am.getCurrentAnimation() : null;
        return EntityRenderData
            .builder(p.getPosition().getX(), p.getPosition().getY(), am)
            .hitbox(p.getHitbox().width, p.getHitbox().height)
            .flipX(!p.isFacingRight())
            .attacking("attack".equals(currentAnim))
            .build();
    }

    /**
     * Converts an {@link EnemyCharacter} to an {@link EntityRenderData}.
     * Includes HP and armour bar values so the renderer can draw status bars.
     *
     * @param e the enemy to convert; must not be {@code null}
     * @return render descriptor for the enemy
     */
    private static EntityRenderData buildEnemy(EnemyCharacter e) {
        AnimationManager am = e.getAnimationManager();
        String currentAnim  = am != null ? am.getCurrentAnimation() : null;
        return EntityRenderData
            .builder(e.getPosition().getX(), e.getPosition().getY(), am)
            .hitbox(e.getHitbox().width, e.getHitbox().height)
            .flipX(!e.isFacingRight())
            .attacking("attack".equals(currentAnim))
            .bars(e.getHp(), e.getMaxHp(), e.getArmor(), e.getMaxArmor())
            .build();
    }

    /**
     * Converts a {@link Duck} to an {@link EntityRenderData}.
     *
     * @param d the duck to convert; must not be {@code null}
     * @return render descriptor for the duck
     */
    private static EntityRenderData buildDuck(Duck d) {
        AnimationManager am = d.getAnimationManager();
        return EntityRenderData
            .builder(d.getPosition().getX(), d.getPosition().getY(), am)
            .hitbox(d.getHitbox().width, d.getHitbox().height)
            .flipX(!d.isFacingRight())
            .build();
    }

    /**
     * Converts a ground {@link Item} to an {@link RenderSnapshot.ItemRenderData}.
     *
     * @param item the item to convert; must not be {@code null}
     * @return render descriptor containing position and icon path
     */
    private static RenderSnapshot.ItemRenderData buildItem(Item item) {
        return new RenderSnapshot.ItemRenderData(
            item.getPosition().getX(),
            item.getPosition().getY(),
            item.getIconPath()
        );
    }

    /**
     * Converts an active {@link Projectile} to an {@link EntityRenderData}.
     *
     * <p>If the projectile implements {@link Renderable}, its own dimensions,
     * offsets, and flip flag are used. Otherwise a default 16×8 rectangle is
     * drawn as a fallback.
     *
     * @param p the projectile to convert; must not be {@code null}
     * @return render descriptor for the projectile
     */
    private static EntityRenderData buildProjectile(Projectile p) {
        if (!(p instanceof Renderable r)) {
            return EntityRenderData
                .builder(p.getPosition().getX(), p.getPosition().getY(), null)
                .renderType(EntityRenderData.RenderType.FIXED_RECT)
                .renderSize(16, 8)
                .build();
        }

        return EntityRenderData
            .builder(p.getPosition().getX(), p.getPosition().getY(), r.getAnimationManager())
            .renderType(EntityRenderData.RenderType.FIXED_RECT)
            .renderSize(r.getRenderWidth(), r.getRenderHeight())
            .renderOffset(r.getRenderOffsetX(), r.getRenderOffsetY())
            .flipX(r.isFlippedX())
            .tint(p.getTintR(), p.getTintG(), p.getTintB())
            .build();
    }
}
