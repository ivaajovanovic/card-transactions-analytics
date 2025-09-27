package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGeneratorService {
    
    private final TransactionService transactionService;
    private final UserActivityService userActivityService;
    
    private static final String[] MERCHANTS = {
            "SuperMart", "CafeX", "GasStation Plus", "BookStore Central", "TechGadgets",
            "Fashion Hub", "Restaurant Deluxe", "Sports Equipment", "Home Depot",
            "Pharmacy Care", "Movie Theater", "Online Store", "Grocery Express",
            "Coffee Corner", "Electronics World"
    };
    
    private static final String[] TRANSACTION_DESCRIPTIONS = {
            "Grocery purchase at %s", "Coffee at %s", "Fuel purchase at %s",
            "Book purchase at %s", "Electronics purchase at %s", "Clothing purchase at %s",
            "Dinner at %s", "Sports equipment at %s", "Home improvement at %s",
            "Pharmacy purchase at %s", "Movie tickets at %s", "Online shopping at %s",
            "Weekly groceries at %s", "Morning coffee at %s", "Tech gadget at %s"
    };
    
    public void generateSampleTransactions(int count) {
        log.info("Generating {} sample transactions", count);
        
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= count; i++) {
            String merchant = MERCHANTS[random.nextInt(MERCHANTS.length)];
            String descriptionTemplate = TRANSACTION_DESCRIPTIONS[random.nextInt(TRANSACTION_DESCRIPTIONS.length)];
            
            Transaction transaction = new Transaction();
            transaction.setId("tx" + String.format("%04d", i));
            transaction.setCardId("card" + (random.nextInt(50) + 1)); // 50 different cards
            transaction.setMerchantId("merchant" + (random.nextInt(20) + 1)); // 20 different merchants
            transaction.setAmount(ThreadLocalRandom.current().nextDouble(10.0, 5000.0));
            transaction.setDate(generateRandomDate());
            transaction.setDescription(String.format(descriptionTemplate, merchant));
            
            transactions.add(transaction);
        }
        
        transactionService.saveAllTransactions(transactions);
        log.info("Successfully generated and saved {} transactions", count);
    }
    
    public void generateSampleUserActivities(int count) {
        log.info("Generating {} sample user activities", count);
        
        List<UserActivity> userActivities = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= count; i++) {
            UserActivity userActivity = new UserActivity();
            userActivity.setUserId("user" + String.format("%03d", i));
            userActivity.setTxCount(random.nextInt(100) + 1); // 1-100 transactions
            userActivity.setLastTxDate(generateRandomDate());
            userActivity.setTotalSpent(ThreadLocalRandom.current().nextDouble(100.0, 10000.0));
            
            userActivities.add(userActivity);
        }
        
        userActivityService.saveAllUserActivities(userActivities);
        log.info("Successfully generated and saved {} user activities", count);
    }
    
    public void generateSampleData() {
        log.info("Generating sample data for testing...");
        generateSampleTransactions(1000);
        generateSampleUserActivities(1000);
        log.info("Sample data generation completed!");
    }
    
    private String generateRandomDate() {
        // Generate dates from last 90 days
        long minDay = LocalDateTime.of(2025, 7, 1, 0, 0)
                .toEpochSecond(ZoneOffset.UTC);
        long maxDay = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(randomDay, 0, ZoneOffset.UTC);
        return dateTime.toString(); // Convert to String in ISO format
    }
}