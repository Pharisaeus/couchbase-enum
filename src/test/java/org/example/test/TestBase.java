package org.example.test;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.QueryScanConsistency;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static org.example.repository.Constants.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestBase {
    static final protected CouchbaseContainer container;

    static {
        container = new CouchbaseContainer("couchbase/server:7.1.1")
                .withStartupTimeout(Duration.ofMinutes(5))
                .withStartupAttempts(1)
                .withBucket(new BucketDefinition(BUCKET))
                .withReuse(false);
        container.start();
    }

    @DynamicPropertySource
    static void couchbaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connection-string", container::getConnectionString);
        registry.add("spring.couchbase.username", container::getUsername);
        registry.add("spring.couchbase.password", container::getPassword);
        registry.add("spring.data.couchbase.bucket-name", () -> BUCKET);
        initCouchbase(container.getConnectionString(), container.getUsername(), container.getPassword());
    }

    public static void initCouchbase(String connectionString, String username, String password) {
        var cluster = Cluster.connect(connectionString, username, password);
        cluster.query("CREATE SCOPE %s.%s IF NOT EXISTS".formatted(BUCKET, SCOPE));
        waitForCondition(cluster, "FROM system:scopes WHERE `path` = \"default:%s.%s\"".formatted(BUCKET, SCOPE));
        cluster.query("CREATE COLLECTION %s.%s.%s IF NOT EXISTS".formatted(BUCKET, SCOPE, COLLECTION));
        waitForCondition(cluster, "FROM system:keyspaces WHERE `path` = \"default:%s.%s.%s\"".formatted(BUCKET, SCOPE, COLLECTION));
        cluster.query("CREATE PRIMARY INDEX IF NOT EXISTS ON %s.%s.%s".formatted(BUCKET, SCOPE, COLLECTION));
        waitForCondition(cluster, "FROM system:indexes WHERE keyspace_id = \"%s\" AND is_primary = true AND state = \"online\"".formatted(COLLECTION));
        cluster.disconnect();
    }

    private static void waitForCondition(Cluster cluster, String condition) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .until(() -> cluster.query("SELECT COUNT(*) = 0 as pending " + condition)
                        .rowsAsObject()
                        .stream()
                        .noneMatch(o -> o.getBoolean("pending")));
    }

    @AfterEach
    public void cleanup() {
        cleanupCouchbase(container.getConnectionString(), container.getUsername(), container.getPassword());
    }

    public static void cleanupCouchbase(String connectionString, String username, String password) {
        var cluster = Cluster.connect(connectionString, username, password);
        cleanupCollection(cluster, COLLECTION);
        cluster.disconnect();
    }

    private static void cleanupCollection(Cluster cluster, String collection) {
        var options = queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS);
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> {
                    cluster.query("DELETE FROM %s.%s.%s".formatted(BUCKET, SCOPE, collection));
                    return cluster.query("SELECT COUNT(*) as count FROM %s.%s.%s".formatted(BUCKET, SCOPE, collection), options)
                            .rowsAsObject()
                            .stream()
                            .map(obj -> obj.getLong("count"))
                            .anyMatch(cnt -> cnt == 0);
                });
    }
}
