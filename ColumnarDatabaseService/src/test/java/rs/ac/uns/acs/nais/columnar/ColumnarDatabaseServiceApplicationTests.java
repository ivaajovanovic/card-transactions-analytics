package rs.ac.uns.acs.nais.columnar;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = { ColumnarDatabaseServiceApplication.class, ColumnarDatabaseServiceApplicationTests.TestCfg.class }
)
@ActiveProfiles("test") // koristi test profil ako imaš override-e u test/resources
class ColumnarDatabaseServiceApplicationTests {

    @Test
    void contextLoads() {
        // samo proverava da se kontekst digne sa mock-ovanom Cassandrom
    }

    @TestConfiguration
    static class TestCfg {
        @Bean
        CqlSession cqlSession() {
            // Mockito mock, da ne pokušava da se kači na pravu Cassandru
            return Mockito.mock(CqlSession.class);
        }
        @Bean
        CqlTemplate cqlTemplate(CqlSession session) {
            return new CqlTemplate(session);
        }
    }
}
