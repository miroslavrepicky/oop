package sk.stuba.fiit.core.engine;

/**
 * Kontrakt pre všetky objekty, ktoré potrebujú byť aktualizované každý snímok.
 *
 * Pôvodný {@code update(float deltaTime)} spôsoboval problém: {@code EnemyCharacter}
 * potreboval ďalšie parametre (platformy, level, hráča), takže musel siahnuť
 * na {@code GameManager} alebo zaviesť iný podpis.
 *
 * Riešenie: {@link UpdateContext} zabalí všetky kontextové dáta do jedného objektu.
 * Každý implementátor si zoberie čo potrebuje a zvyšok ignoruje, bez závislosti
 * na {@code GameManager}.
 *
 * Spätná kompatibilita: {@code update(float)} zostáva ako {@code default} metóda,
 * ktorá vytvorí minimálny kontext a deleguje. Triedy, ktoré ho override-li,
 * môžu prejsť na {@code update(UpdateContext)} postupne.
 */
public interface Updatable {

    /**
     * Hlavná update metóda.
     *
     * @param ctx kontext snímka – obsahuje deltaTime, platformy, level, hráča
     */
    void update(UpdateContext ctx);

    /**
     * Spätne kompatibilná metóda – vytvára minimálny kontext (len deltaTime).
     * Override len ak má objekt naozaj všetky dáta sám (napr. animácie, UI).
     *
     * @deprecated Použi {@link #update(UpdateContext)}
     */
    @Deprecated
    default void update(float deltaTime) {
        update(new UpdateContext(deltaTime));
    }
}
