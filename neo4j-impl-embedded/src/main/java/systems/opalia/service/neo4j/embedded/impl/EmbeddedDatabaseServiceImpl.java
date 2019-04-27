package systems.opalia.service.neo4j.embedded.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryRow;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryStatistics;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryTable;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseService;


public final class EmbeddedDatabaseServiceImpl
        implements EmbeddedDatabaseService {

    private final GraphDatabaseService database;

    EmbeddedDatabaseServiceImpl(GraphDatabaseService database) {

        this.database = database;
    }

    public EmbeddedDatabaseQueryTable execute(String clause, Map<String, Object> parameters) {

        Result result = database.execute(clause, parameters);

        EmbeddedDatabaseQueryStatistics statistics =
                new EmbeddedDatabaseQueryStatisticsImpl(result.getQueryStatistics());

        List<String> columns = result.columns();

        List<EmbeddedDatabaseQueryRow> rows =
                result.stream()
                        .map((x) -> new EmbeddedDatabaseQueryRowImpl(columns, x))
                        .collect(Collectors.toList());

        return new EmbeddedDatabaseQueryTableImpl(statistics, columns, rows);
    }
}
