package rs.ac.uns.acs.nais.ColumnarDatabaseService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ColumnarDatabaseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ColumnarDatabaseServiceApplication.class, args);
	}

}
