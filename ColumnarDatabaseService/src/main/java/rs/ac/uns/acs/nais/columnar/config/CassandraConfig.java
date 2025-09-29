package rs.ac.uns.acs.nais.columnar.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.Supplier;

@Configuration
@EnableCassandraRepositories(basePackages = "rs.ac.uns.acs.nais.columnar.repo")
@Slf4j
public class CassandraConfig {

    @Value("${spring.data.cassandra.contact-points:localhost}")
    private String contactPoints;
    
    @Value("${spring.data.cassandra.port:9042}")
    private int port;
    
    @Value("${spring.data.cassandra.keyspace-name:nais}")
    private String keyspaceName;

    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

    /**
     * Custom CqlSession that ensures keyspace exists before connecting to it
     */
    @Bean
    @Primary
    public CqlSession cqlSession() {
        log.info("=== CREATING CUSTOM CASSANDRA SESSION ===");
        log.info("Contact Points: {}", contactPoints);
        log.info("Port: {}", port);
        log.info("Keyspace: {}", keyspaceName);
        log.info("Datacenter: {}", localDatacenter);

        // First, create session without keyspace to create keyspace if needed
        CqlSession bootstrapSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(localDatacenter)
                .build();

        try {
            // Create keyspace if it doesn't exist
            String createKeyspace = String.format(
                "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}",
                keyspaceName
            );
            log.info("Ensuring keyspace exists: {}", keyspaceName);
            bootstrapSession.execute(createKeyspace);
            log.info("Keyspace {} ensured", keyspaceName);
        } finally {
            bootstrapSession.close();
        }

        // Now create session with keyspace
        CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(localDatacenter)
                .withKeyspace(keyspaceName)
                .build();

        log.info("=== CASSANDRA SESSION CREATED SUCCESSFULLY ===");
        return session;
    }

    /**
     * CqlTemplate za direktne CQL upite (npr. counter UPDATE).
     * Spring Boot već kreira CqlSession na osnovu application.properties,
     * ovde ga samo "uvodimo" u template.
     */
    @Bean
    public CqlTemplate cqlTemplate(CqlSession session) {
        return new CqlTemplate(session);
    }

    /**
     * Dodatni customizer da smo sigurni da je local-datacenter postavljen,
     * čak i ako env varijable pregaze properties.
     */
    @Bean
    public CqlSessionBuilderCustomizer datacenterCustomizer() {
        return builder -> builder.withLocalDatacenter(localDatacenter);
    }

    /**
     * Helper za generisanje timeuuid vrednosti (korisno u servisima).
     */
    @Bean
    public Supplier<UUID> timeUuidSupplier() {
        return Uuids::timeBased;
    }
}
