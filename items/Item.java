package sk.stuba.fiit.items;

import sk.stuba.fiit.characters.PlayerCharacter;
import sk.stuba.fiit.core.Pickable;
import sk.stuba.fiit.core.Updatable;
import sk.stuba.fiit.util.Vector2D;

import com.badlogic.gdx.math.Rectangle;


public abstract class Item implements Pickable, Updatable {
    protected int slotCost;
    protected Vector2D position;
    protected Rectangle hitbox;


    public Item(int slotCost, Vector2D position) {
        this.slotCost = slotCost;
        this.position = position;
        this.hitbox = new Rectangle(position.getX(), position.getY(), 32, 32);
    }

    public abstract void use(PlayerCharacter character);

    /**
     * Vrati cestu k ikone itemu (napr. „icons/duck.png“).
     * Ked item lezi na zemi, GameRenderer ju vykresli na jeho pozicii.
     * Vrat null ak item nema ikonu.
     */
    public abstract String getIconPath();

    @Override
    public void onPickup(PlayerCharacter character) {
        character.getInventory().addItem(this);
    }

    @Override
    public void update(float deltaTime) {
        // predmety sa nehybu, override ak treba
    }

    public int getSlotsRequired() { return slotCost; }
    public Vector2D getPosition() { return position; }
    public Rectangle getHitbox() { return hitbox; }
}
