package sk.stuba.fiit.core;

/**
 * Kontrakt pre každý herný stav v rámci State vzoru.
 *
 * Dôvod existencie: logika pre PLAYING, PAUSED, GAME_OVER_DELAY
 * bola rozhádzaná v if/switch blokoch cez GameScreen.render()
 * a GameManager.update(). Každý stav teraz zapuzdrí svoju vlastnú
 * logiku a vie, na ktorý stav má prejsť.
 *
 * Tok:
 *   GameScreen drží currentState : IGameState
 *   každý frame volá currentState.update() a currentState.render()
 *   ak currentState.next() != null, prepne stav
 */
public interface IGameState {

    /**
     * Herná logika pre tento stav (pohyb, AI, kolízie...).
     * @param deltaTime čas od posledného snímka
     */
    void update(float deltaTime);

    /**
     * Vykresľovanie špecifické pre tento stav (napr. overlay, fade...).
     * Základné vykresľovanie levelu rieši GameRenderer – stav ho môže
     * doplniť (pauza = stmavenie, game over = červený tint...).
     */
    void render(float deltaTime);

    /**
     * Vráti nasledujúci stav alebo null ak sa zostáva v aktuálnom.
     * GameScreen toto kontroluje po každom update() a ak dostane
     * non-null hodnotu, vykoná prechod.
     */
    IGameState next();
}
