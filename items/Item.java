package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.engine.Pickable;
import sk.stuba.fiit.core.engine.Updatable;
import sk.stuba.fiit.core.engine.UpdateContext;
import sk.stuba.fiit.inventory.Inventory;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

import com.badlogic.gdx.math.Rectangle;


/**
 * Base class for all inventory and world items.
 *
 * <p>Items exist in two contexts:
 * <ol>
 *   <li><b>World</b> – lying on the ground with a hitbox; the player walks over them
 *       and presses E to pick up.</li>
 *   <li><b>Inventory</b> – stored in an inventory slot; the player selects and uses them.</li>
 * </ol>
 *
 * <p>The {@code level} parameter in {@link #use(PlayerCharacter, Level)} lets the item
 * interact with the game world (e.g. spawn projectiles) without calling {@code GameManager}.
 */
public abstract class Item implements Pickable, Updatable {
    protected int slotCost;
    protected Vector2D position;
    protected Rectangle hitbox;

    public Item(int slotCost, Vector2D position) {
        this.slotCost = slotCost;
        this.position = position;
        this.hitbox   = new Rectangle(position.getX(), position.getY(), 32, 32);
    }

    /**
     * Uses the item, applying its effect to the character.
     *
     * @param character the player using the item
     * @param level     the current level; may be {@code null} if the item does not need it
     */
    public abstract void use(PlayerCharacter character, Level level, Inventory inventory);

    /**
     * Returns the path to the item's inventory icon, e.g. {@code "icons/potion.png"}.
     * Return {@code null} if the item has no icon.
     *
     * @return icon path or {@code null}
     */
    public abstract String getIconPath();

    @Override
    public boolean onPickup(PlayerCharacter character, Inventory inventory) {
        return inventory.addItem(this);
    }

    @Override
    public void update(UpdateContext ctx) {
        // predmety sa nehýbu, override ak treba
    }

    public int      getSlotsRequired() { return slotCost; }
    public Vector2D getPosition()      { return position; }
    public Rectangle getHitbox()       { return hitbox; }
}
