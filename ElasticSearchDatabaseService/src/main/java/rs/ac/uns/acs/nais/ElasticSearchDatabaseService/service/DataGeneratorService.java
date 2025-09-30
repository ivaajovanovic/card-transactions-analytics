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
            "Weekly groceries at %s", "Morning coffee at %s", "Tech gadget at %s",
            "Lunch at %s", "Breakfast at %s", "Snacks at %s", "Beverages at %s",
            "Fashion accessories at %s", "Shoes at %s", "Jewelry at %s",
            "Beauty products at %s", "Health supplements at %s", "Medical supplies at %s",
            "Garden supplies at %s", "Pet supplies at %s", "Baby products at %s",
            "Office supplies at %s", "School supplies at %s", "Art supplies at %s",
            "Music equipment at %s", "Gaming accessories at %s", "Camera gear at %s",
            "Fitness equipment at %s", "Yoga mat at %s", "Protein powder at %s",
            "Travel booking at %s", "Hotel reservation at %s", "Car rental at %s",
            "Flight tickets at %s", "Train tickets at %s", "Bus tickets at %s",
            "Taxi fare at %s", "Ride sharing at %s", "Parking fee at %s",
            "Insurance payment at %s", "Utility bill at %s", "Internet service at %s",
            "Phone bill at %s", "Cable TV at %s", "Streaming service at %s",
            "Gym membership at %s", "Fitness class at %s", "Personal trainer at %s",
            "Haircut at %s", "Manicure at %s", "Massage at %s",
            "Dry cleaning at %s", "Laundry service at %s", "Tailoring at %s",
            "Car maintenance at %s", "Oil change at %s", "Tire replacement at %s",
            "Car wash at %s", "Detailing service at %s", "Auto repair at %s",
            "Home cleaning at %s", "Pest control at %s", "Landscaping at %s",
            "Plumbing service at %s", "Electrical work at %s", "HVAC service at %s",
            "Appliance repair at %s", "Furniture delivery at %s", "Moving service at %s",
            "Legal consultation at %s", "Tax preparation at %s", "Financial planning at %s",
            "Real estate service at %s", "Property management at %s", "Home inspection at %s",
            "Event planning at %s", "Catering service at %s", "Photography at %s",
            "Wedding planning at %s", "Party supplies at %s", "Decoration at %s",
            "Gift purchase at %s", "Flower delivery at %s", "Balloon bouquet at %s",
            "Subscription box at %s", "Magazine subscription at %s", "Newspaper delivery at %s",
            "Charity donation at %s", "Fundraising event at %s", "Volunteer supplies at %s",
            "Educational course at %s", "Language learning at %s", "Skill development at %s",
            "Professional certification at %s", "Conference ticket at %s", "Workshop fee at %s",
            "Software license at %s", "Cloud storage at %s", "Domain registration at %s",
            "Website hosting at %s", "Digital marketing at %s", "SEO service at %s",
            "Social media ads at %s", "Content creation at %s", "Video production at %s",
            "Podcast hosting at %s", "Music streaming at %s", "Video streaming at %s",
            "Gaming subscription at %s", "Virtual reality at %s", "Augmented reality at %s",
            "Cryptocurrency at %s", "Stock trading at %s", "Investment platform at %s",
            "Crowdfunding at %s", "Peer lending at %s", "Microfinance at %s",
            "Freelance platform at %s", "Gig economy at %s", "Task marketplace at %s",
            "Food delivery at %s", "Grocery delivery at %s", "Meal kit at %s",
            "Wine tasting at %s", "Craft beer at %s", "Cocktail ingredients at %s",
            "Cooking class at %s", "Wine pairing at %s", "Chef service at %s",
            "Food truck at %s", "Street food at %s", "Food festival at %s",
            "Farmers market at %s", "Organic produce at %s", "Local artisan at %s",
            "Handmade crafts at %s", "Vintage items at %s", "Antique collection at %s",
            "Collectibles at %s", "Trading cards at %s", "Comic books at %s",
            "Board games at %s", "Puzzle games at %s", "Educational toys at %s",
            "Outdoor gear at %s", "Camping equipment at %s", "Hiking supplies at %s",
            "Fishing tackle at %s", "Hunting gear at %s", "Water sports at %s",
            "Winter sports at %s", "Ski equipment at %s", "Snowboard gear at %s",
            "Summer camp at %s", "Sports camp at %s", "Music camp at %s",
            "Art camp at %s", "Science camp at %s", "Adventure camp at %s"
    };
    
    public void generateSampleTransactions(int count) {
        log.info("Generating {} sample transactions", count);
        
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();
        
        // Create user-to-cards mapping: each user has 1-3 cards
        Map<String, List<String>> userCardsMap = createUserCardsMapping();
        
        for (int i = 1; i <= count; i++) {
            String merchant = MERCHANTS[random.nextInt(MERCHANTS.length)];
            String descriptionTemplate = TRANSACTION_DESCRIPTIONS[random.nextInt(TRANSACTION_DESCRIPTIONS.length)];
            
            // Select a random user and one of their cards
            String[] userIds = userCardsMap.keySet().toArray(new String[0]);
            String selectedUserId = userIds[random.nextInt(userIds.length)];
            List<String> userCards = userCardsMap.get(selectedUserId);
            String selectedCardId = userCards.get(random.nextInt(userCards.size()));
            
            Transaction transaction = new Transaction();
            transaction.setId("tx" + String.format("%04d", i));
            transaction.setCardId(selectedCardId); // Set the cardId for this user
            transaction.setMerchantId("merchant" + (random.nextInt(20) + 1)); // 20 different merchants
            transaction.setAmount(ThreadLocalRandom.current().nextDouble(10.0, 5000.0));
            transaction.setDate(generateRandomDate());
            transaction.setDescription(String.format(descriptionTemplate, merchant));
            
            transactions.add(transaction);
        }
        
        transactionService.saveAllTransactions(transactions);
        log.info("Successfully generated and saved {} transactions", count);
    }
    
    /**
     * Creates a mapping where each user has 1-3 cards
     * Users: user001, user002, ..., user3000
     * Cards: card1, card2, ..., card50 (distributed among users)
     */
    private Map<String, List<String>> createUserCardsMapping() {
        Map<String, List<String>> userCardsMap = new HashMap<>();
        Random random = new Random();
        
        // Generate 3000 users
        for (int i = 1; i <= 3000; i++) {
            String userId = "user" + String.format("%03d", i);
            List<String> userCards = new ArrayList<>();
            
            // Each user gets 1-3 cards
            int cardCount = random.nextInt(3) + 1; // 1, 2, or 3 cards
            
            for (int j = 0; j < cardCount; j++) {
                // Generate unique card IDs (card1 to card50)
                String cardId = "card" + (random.nextInt(50) + 1);
                if (!userCards.contains(cardId)) {
                    userCards.add(cardId);
                }
            }
            
            userCardsMap.put(userId, userCards);
        }
        
        return userCardsMap;
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
        generateSampleTransactions(15000); // Increased from 5000 to 15000
        generateSampleUserActivities(3000); // Increased from 2000 to 3000
        log.info("Sample data generation completed!");
    }
    
    private String generateRandomDate() {
        // Generate dates from last 3 months (90 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        
        long minDay = threeMonthsAgo.toEpochSecond(ZoneOffset.UTC);
        long maxDay = now.toEpochSecond(ZoneOffset.UTC);
        
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(randomDay, 0, ZoneOffset.UTC);
        return dateTime.toString(); // Convert to String in ISO format
    }
}