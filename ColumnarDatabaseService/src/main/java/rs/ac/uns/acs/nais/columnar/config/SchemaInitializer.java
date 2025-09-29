package rs.ac.uns.acs.nais.columnar.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaInitializer {

    private final ResourceLoader resourceLoader;
    private final CqlTemplate cqlTemplate;

    @PostConstruct
    public void applySchema() {
        try {
            Resource res = resourceLoader.getResource("classpath:db/cql/schema.cql");
            if (!res.exists()) {
                log.warn("Cassandra schema resource not found: {}", res);
                return;
            }

            log.info("Applying Cassandra schema from class path resource [db/cql/schema.cql]");
            String cql = StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);

            // Prođi kroz sve CQL statement-e razdvojene ';'
            for (String stmt : cql.split(";")) {
                String s = stmt.trim();
                if (s.isEmpty() || s.startsWith("--") || s.startsWith("/*")) {
                    continue;
                }
                cqlTemplate.execute(s);
            }
            log.info("Cassandra schema applied.");
        } catch (Exception e) {
            log.error("Failed to apply Cassandra schema", e);
            throw new RuntimeException("Failed to apply Cassandra schema", e);
        }
    }
}
