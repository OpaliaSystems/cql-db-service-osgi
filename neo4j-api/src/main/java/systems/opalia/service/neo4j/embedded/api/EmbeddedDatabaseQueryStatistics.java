package systems.opalia.service.neo4j.embedded.api;


public interface EmbeddedDatabaseQueryStatistics {

    // Returns the number of nodes created by this query.
    int getNodesCreated();

    // Returns the number of nodes deleted by this query.
    int getNodesDeleted();

    // Returns the number of relationships created by this query.
    int getRelationshipsCreated();

    // Returns the number of relationships deleted by this query.
    int getRelationshipsDeleted();

    // Returns the number of properties (same again or other) set by this query.
    int getPropertiesSet();

    // Returns the number of labels added to any node by this query.
    int getLabelsAdded();

    // Returns the number of labels removed from any node by this query.
    int getLabelsRemoved();

    // Returns the number of indexes added by this query.
    int getIndexesAdded();

    // Returns the number of indexes removed by this query.
    int getIndexesRemoved();

    // Returns the number of constraints added by this query.
    int getConstraintsAdded();

    // Returns the number of constraints removed by this query.
    int getConstraintsRemoved();

    // If the query updated the graph in any way, this method will return true.
    boolean containsUpdates();
}
