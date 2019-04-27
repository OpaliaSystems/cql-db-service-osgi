package systems.opalia.service.neo4j.embedded.api;

import java.nio.file.Path;


public interface EmbeddedDatabaseFactory {

    EmbeddedDatabase newEmbeddedDatabase(Path configFile,
                                         Path storeDirectory,
                                         EmbeddedDatabaseLoggingProvider loggingProvider,
                                         boolean dbHighlyAvailable,
                                         int txRetries,
                                         long txBackoff);

    EmbeddedDatabaseBackup newEmbeddedDatabaseBackup(Path storeDirectory, String hostname, int port);
}
