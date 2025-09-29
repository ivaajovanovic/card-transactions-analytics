package rs.ac.uns.acs.nais.columnar.config;

import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@Profile("docker")
public class CassandraConfigOverride {

    @Bean
    @Primary
    public CassandraProperties cassandraProperties() {
        CassandraProperties properties = new CassandraProperties();
        
        // Force cassandradb as contact point
        properties.setContactPoints(Arrays.asList("cassandradb"));
        properties.setPort(9042);
        properties.setKeyspaceName("nais");
        properties.setLocalDatacenter("datacenter1");
        // properties.setSchemaAction(CassandraProperties.SchemaAction.CREATE_IF_NOT_EXISTS);
        
        System.out.println("=== FORCED CASSANDRA CONFIG ===");
        System.out.println("Contact Points: " + properties.getContactPoints());
        System.out.println("Port: " + properties.getPort());
        System.out.println("Keyspace: " + properties.getKeyspaceName());
        System.out.println("Datacenter: " + properties.getLocalDatacenter());
        System.out.println("=================================");
        
        return properties;
    }
}