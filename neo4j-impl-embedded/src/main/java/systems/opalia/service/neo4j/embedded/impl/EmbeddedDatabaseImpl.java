package systems.opalia.service.neo4j.embedded.impl;

import java.nio.file.Path;
import java.util.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.DeadlockDetectedException;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabase;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseLoggingProvider;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseService;


public final class EmbeddedDatabaseImpl
        implements EmbeddedDatabase {

    private final GraphDatabaseService database;
    private final int txRetries;
    private final long txBackoff;

    EmbeddedDatabaseImpl(Path configFile,
                         Path storeDirectory,
                         EmbeddedDatabaseLoggingProvider loggingProvider,
                         boolean dbHighlyAvailable,
                         int txRetries,
                         long txBackoff) {

        GraphDatabaseBuilder builder;

        if (dbHighlyAvailable)
            builder =
                    new HighlyAvailableGraphDatabaseFactory()
                            .setUserLogProvider(new EmbeddedDatabaseLoggingProviderAdapter(loggingProvider))
                            .newEmbeddedDatabaseBuilder(storeDirectory.toFile());
        else
            builder =
                    new GraphDatabaseFactory()
                            .setUserLogProvider(new EmbeddedDatabaseLoggingProviderAdapter(loggingProvider))
                            .newEmbeddedDatabaseBuilder(storeDirectory.toFile());

        builder.loadPropertiesFromFile(configFile.toString());

        this.database = builder.newGraphDatabase();
        this.txRetries = txRetries;
        this.txBackoff = txBackoff;
    }

    public boolean waitAvailable(long timeout) {

        return database.isAvailable(timeout);
    }

    public void shutdown() {

        database.shutdown();
    }

    public <T> T withTransaction(Function<EmbeddedDatabaseService, T> block)
            throws Throwable {

        EmbeddedDatabaseService service = new EmbeddedDatabaseServiceImpl(database);

        int retries = txRetries;
        Throwable throwable;

        do {

            Transaction tx;

            synchronized (this) {

                tx = database.beginTx();
            }

            try {

                T result = block.apply(service);

                tx.success();

                return result;

            } catch (DeadlockDetectedException | TransactionTerminatedException e) {

                throwable = e;
                Thread.sleep(txBackoff);

            } catch (Throwable e) {

                retries = 0;
                throwable = e;
                tx.failure();

            } finally {

                tx.close();
            }

        } while (retries-- > 0);

        throw throwable;
    }
}
