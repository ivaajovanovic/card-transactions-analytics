package rs.ac.uns.acs.nais.columnar.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Konfiguracija za REST komunikaciju između mikroservisa
 * Omogućava service discovery preko Eureka servera
 */
@Configuration
public class RestClientConfig {

    /**
     * RestTemplate sa load balancing za komunikaciju sa drugim mikroservisima
     * @LoadBalanced omogućava korišćenje service imena umesto IP adresa
     */
    @Bean
    @LoadBalanced
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Direktni RestTemplate za HTTP pozive bez load balancing-a
     * Koristi se za direktne pozive sa IP adresama ili hostname-ovima
     */
    @Bean("directRestTemplate")
    public RestTemplate directRestTemplate() {
        return new RestTemplate();
    }
}