package sk.stuba.fiit.core;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centrálny logger wrapper.
 *
 * Poskytuje:
 *  - štandardné SLF4J loggery cez get()
 *  - helper metódy pre štruktúrované (strojovo spracovateľné) logy
 *  - guard-check pattern pre hot path
 */
public final class GameLogger {

    private GameLogger() {}

    public static Logger get(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Štruktúrovaný log – vypíše kľúč-hodnota páry do JSON poľa.
     * Výsledok v JSON súbore:
     * { "message": "Enemy spawned", "enemy": "EnemyKnight", "x": 120.0, "y": 64.0 }
     */
    public static void structured(Logger logger, String message, Object... keyValues) {
        if (!logger.isInfoEnabled()) return;
        // StructuredArguments.kv() pridá pár ako samostatné JSON pole
        Object[] args = new Object[keyValues.length / 2];
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            args[i / 2] = StructuredArguments.kv(
                keyValues[i].toString(), keyValues[i + 1]);
        }
        logger.info(message, args);
    }
}
