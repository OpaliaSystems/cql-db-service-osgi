package systems.opalia.service.neo4j.embedded.api;


public interface EmbeddedDatabaseLoggingProvider {

    EmbeddedDatabaseLogger getLogger(String name);
}
