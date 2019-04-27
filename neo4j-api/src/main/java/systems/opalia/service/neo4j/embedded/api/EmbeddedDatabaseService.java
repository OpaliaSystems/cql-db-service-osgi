package systems.opalia.service.neo4j.embedded.api;

import java.util.Map;


public interface EmbeddedDatabaseService {

    EmbeddedDatabaseQueryTable execute(String clause, Map<String, Object> parameters);
}
