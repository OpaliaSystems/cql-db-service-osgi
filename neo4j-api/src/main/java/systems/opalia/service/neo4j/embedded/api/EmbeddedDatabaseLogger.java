package systems.opalia.service.neo4j.embedded.api;


public interface EmbeddedDatabaseLogger {

    boolean isDebugEnabled();

    boolean isInfoEnabled();

    boolean isWarningEnabled();

    boolean isErrorEnabled();

    void debug(String message);

    void debug(String message, Throwable throwable);

    void info(String message);

    void info(String message, Throwable throwable);

    void warning(String message);

    void warning(String message, Throwable throwable);

    void error(String message);

    void error(String message, Throwable throwable);
}
