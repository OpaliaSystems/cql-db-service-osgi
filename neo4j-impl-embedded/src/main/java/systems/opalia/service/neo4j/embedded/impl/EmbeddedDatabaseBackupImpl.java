package systems.opalia.service.neo4j.embedded.impl;

import java.nio.file.Path;
import org.neo4j.backup.OnlineBackup;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseBackup;


public final class EmbeddedDatabaseBackupImpl
        implements EmbeddedDatabaseBackup {

    private final Path storeDirectory;
    private final String hostname;
    private final int port;

    EmbeddedDatabaseBackupImpl(Path storeDirectory, String hostname, int port) {

        this.storeDirectory = storeDirectory;
        this.hostname = hostname;
        this.port = port;
    }

    public void backup() {

        OnlineBackup onlineBackup = OnlineBackup.from(hostname, port);

        onlineBackup.backup(storeDirectory.toFile(), true);
    }
}
