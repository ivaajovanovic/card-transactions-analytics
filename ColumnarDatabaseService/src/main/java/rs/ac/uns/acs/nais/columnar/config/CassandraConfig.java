package rs.ac.uns.acs.nais.columnar.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
@EnableCassandraRepositories(basePackages = "rs.ac.uns.acs.nais.columnar.repo")
public class CassandraConfig {

    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;

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
