package systems.opalia.service.neo4j.embedded.impl;

import java.util.ArrayList;
import java.util.List;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryRow;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryStatistics;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryTable;


public final class EmbeddedDatabaseQueryTableImpl
        implements EmbeddedDatabaseQueryTable {

    private final EmbeddedDatabaseQueryStatistics statistics;
    private final List<String> columns;
    private final List<EmbeddedDatabaseQueryRow> rows;

    EmbeddedDatabaseQueryTableImpl(EmbeddedDatabaseQueryStatistics statistics,
                                   List<String> columns,
                                   List<EmbeddedDatabaseQueryRow> rows) {

        this.statistics = statistics;
        this.columns = columns;
        this.rows = rows;
    }

    public EmbeddedDatabaseQueryStatistics getStatistics() {

        return statistics;
    }

    public List<String> getColumns() {

        return new ArrayList<>(columns);
    }

    public List<EmbeddedDatabaseQueryRow> getRows() {

        return new ArrayList<>(rows);
    }
}
