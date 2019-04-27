package systems.opalia.service.neo4j.embedded.api;

import java.util.List;


public interface EmbeddedDatabaseQueryTable {

    EmbeddedDatabaseQueryStatistics getStatistics();

    List<String> getColumns();

    List<EmbeddedDatabaseQueryRow> getRows();
}
