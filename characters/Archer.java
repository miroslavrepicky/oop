package sk.stuba.fiit.characters;

import sk.stuba.fiit.core.AnimationManager;
import sk.stuba.fiit.core.GameManager;
import sk.stuba.fiit.core.NormalGravity;
import sk.stuba.fiit.projectiles.Arrow;
import sk.stuba.fiit.util.Vector2D;
import sk.stuba.fiit.world.Level;

public class Archer extends PlayerCharacter {
    private int arrowCount;

    public Archer(Vector2D position) {
        super("Archer", 80, 20, 3.5f, position);
        this.arrowCount = 30;
        this.gravityStrategy = new NormalGravity();

    }

    @Override
    public void performAttack() {
        if (arrowCount <= 0) return;

        Level level = GameManager.getInstance().getCurrentLevel();
        if (level == null) return;

        arrowCount--;
        float dirX = isFacingRight() ? 1 : -1;
        Vector2D direction = new Vector2D(dirX, 0);
        Arrow arrow = new Arrow(attackPower, 8.0f,
            new Vector2D(position.getX(), position.getY()),
            direction, false);
        level.addProjectile(arrow);
    }

    @Override
    public AnimationManager getAnimationManager() {
        return null;
    }

    public Arrow shootArrow() {
        arrowCount--;
        Vector2D direction = new Vector2D(1, 0); // smer strelu
        return new Arrow(attackPower, 5.0f, position, direction, false);
    }

    @Override
    public void handleInput() {
        // spracovanie vstupu hráča
    }

    @Override
    public void update(float deltaTime) {
        handleInput();
    }

    public int getArrowCount() { return arrowCount; }
}
