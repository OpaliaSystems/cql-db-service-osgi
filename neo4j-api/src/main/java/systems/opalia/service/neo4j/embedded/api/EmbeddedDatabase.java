package systems.opalia.service.neo4j.embedded.api;

import java.util.function.Function;


public interface EmbeddedDatabase {

    boolean waitAvailable(long timeout);

    void shutdown();

    <T> T withTransaction(Function<EmbeddedDatabaseService, T> block) throws Throwable;
}
