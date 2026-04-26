package sk.stuba.fiit.core;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logger wrapper providing SLF4J loggers and structured logging helpers.
 *
 * <p>Features:
 * <ul>
 *   <li>Standard SLF4J loggers via {@link #get(Class)}.</li>
 *   <li>Structured (machine-readable) key-value log entries via {@link #structured}.</li>
 *   <li>Guard-check pattern for hot-path logging (check {@code isDebugEnabled()} first).</li>
 * </ul>
 */
public final class GameLogger {

    private GameLogger() {}

    /**
     * Returns an SLF4J {@link Logger} for the given class.
     *
     * @param clazz the class requesting a logger
     * @return SLF4J logger instance
     */
    public static Logger get(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Logs a structured INFO message with key-value pairs as separate JSON fields.
     * Example output: {@code { "message": "Enemy spawned", "enemy": "EnemyKnight", "x": 120.0 }}
     *
     * @param logger    the logger to write to
     * @param message   the human-readable log message
     * @param keyValues alternating key/value pairs (must be an even count)
     */
    public static void structured(Logger logger, String message, Object... keyValues) {
        if (!logger.isInfoEnabled()) return;
        // StructuredArguments.kv() prida par ako samostatne JSON pole
        Object[] args = new Object[keyValues.length / 2];
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            args[i / 2] = StructuredArguments.kv(
                keyValues[i].toString(), keyValues[i + 1]);
        }
        logger.info(message, args);
    }
}


