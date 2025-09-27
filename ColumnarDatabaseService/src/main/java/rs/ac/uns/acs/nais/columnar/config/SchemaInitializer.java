package rs.ac.uns.acs.nais.columnar.config;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.schema.init", havingValue = "true", matchIfMissing = false)
public class SchemaInitializer implements CommandLineRunner {

    private final CqlSession session;

    @Value("classpath:db/cql/schema.cql")
    private Resource schema;

    public SchemaInitializer(CqlSession session) {
        this.session = session;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Applying Cassandra schema from {}", schema);
        try (var is = schema.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder stmt = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                stmt.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String cql = stmt.toString();
                    stmt.setLength(0);
                    session.execute(cql);
                }
            }
        }
        log.info("Cassandra schema applied.");
    }
}
