package systems.opalia.service.neo4j.embedded.impl;

import java.nio.file.Path;
import org.osgi.service.component.annotations.Component;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabase;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseBackup;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseFactory;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseLoggingProvider;


@Component(service = EmbeddedDatabaseFactory.class)
public final class EmbeddedDatabaseFactoryImpl
        implements EmbeddedDatabaseFactory {

    public EmbeddedDatabase newEmbeddedDatabase(Path configFile,
                                                Path storeDirectory,
                                                EmbeddedDatabaseLoggingProvider loggingProvider,
                                                boolean dbHighlyAvailable,
                                                int txRetries,
                                                long txBackoff) {

        return new EmbeddedDatabaseImpl(
                configFile,
                storeDirectory,
                loggingProvider,
                dbHighlyAvailable,
                txRetries,
                txBackoff);
    }

    public EmbeddedDatabaseBackup newEmbeddedDatabaseBackup(Path storeDirectory, String hostname, int port) {

        return new EmbeddedDatabaseBackupImpl(storeDirectory, hostname, port);
    }
}
