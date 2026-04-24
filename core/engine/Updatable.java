package sk.stuba.fiit.core.engine;

/**
 * Contract for all objects that need to be updated every frame.
 *
 * <p>The original {@code update(float deltaTime)} signature caused problems because
 * {@code EnemyCharacter} needed additional parameters (platforms, level, player),
 * which forced either a dependency on {@code GameManager} or a non-standard signature.
 *
 * <p>Solution: {@link UpdateContext} bundles all contextual data into one object.
 * Each implementor takes what it needs and ignores the rest, without depending
 * on {@code GameManager}.
 *
 * <p>Backward compatibility: {@code update(float)} remains as a {@code default}
 * method that creates a minimal context and delegates. Classes that previously
 * overrode it can migrate to {@code update(UpdateContext)} incrementally.
 */
public interface Updatable {

    /**
     * Main update method called once per frame.
     *
     * @param ctx frame context containing deltaTime, platforms, level, and player
     */
    void update(UpdateContext ctx);
}
