package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.TransactionService;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/health")
    public String health() {
        return "Elasticsearch Service is running!";
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/transaction-service")
    public String testTransactionService() {
        try {
            TransactionService transactionService = applicationContext.getBean(TransactionService.class);
            return "SUCCESS: TransactionService is available - " + transactionService.getClass().getSimpleName();
        } catch (Exception e) {
            return "ERROR: TransactionService NOT found in context - " + e.getMessage();
        }
    }
}