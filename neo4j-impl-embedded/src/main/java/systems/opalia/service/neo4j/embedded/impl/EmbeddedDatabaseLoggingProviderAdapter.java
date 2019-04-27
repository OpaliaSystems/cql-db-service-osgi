package systems.opalia.service.neo4j.embedded.impl;

import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseLoggingProvider;


public final class EmbeddedDatabaseLoggingProviderAdapter
        implements LogProvider {

    private final EmbeddedDatabaseLoggingProvider loggingProvider;

    public EmbeddedDatabaseLoggingProviderAdapter(EmbeddedDatabaseLoggingProvider loggingProvider) {

        this.loggingProvider = loggingProvider;
    }

    public Log getLog(String name) {

        return new EmbeddedDatabaseLoggerAdapter(loggingProvider.getLogger(name));
    }

    public Log getLog(Class clazz) {

        return new EmbeddedDatabaseLoggerAdapter(loggingProvider.getLogger(clazz.getName()));
    }
}
