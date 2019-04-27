package systems.opalia.service.neo4j.embedded.impl;

import org.neo4j.graphdb.QueryStatistics;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryStatistics;


public final class EmbeddedDatabaseQueryStatisticsImpl
        implements EmbeddedDatabaseQueryStatistics {

    private final QueryStatistics statistics;

    EmbeddedDatabaseQueryStatisticsImpl(QueryStatistics statistics) {

        this.statistics = statistics;
    }

    public int getNodesCreated() {

        return statistics.getNodesCreated();
    }

    public int getNodesDeleted() {

        return statistics.getNodesDeleted();
    }

    public int getRelationshipsCreated() {

        return statistics.getRelationshipsCreated();
    }

    public int getRelationshipsDeleted() {

        return statistics.getRelationshipsDeleted();
    }

    public int getPropertiesSet() {

        return statistics.getPropertiesSet();
    }

    public int getLabelsAdded() {

        return statistics.getLabelsAdded();
    }

    public int getLabelsRemoved() {

        return statistics.getLabelsRemoved();
    }

    public int getIndexesAdded() {

        return statistics.getIndexesAdded();
    }

    public int getIndexesRemoved() {

        return statistics.getIndexesRemoved();
    }

    public int getConstraintsAdded() {

        return statistics.getConstraintsAdded();
    }

    public int getConstraintsRemoved() {

        return statistics.getConstraintsRemoved();
    }

    public boolean containsUpdates() {

        return statistics.containsUpdates();
    }
}
