package rs.ac.uns.acs.nais.GraphDatabaseService.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SampleDataGenerator {
    
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final AcceptanceRepository acceptanceRepository;
    private final RewardProgramRepository rewardProgramRepository;
    private final RewardRuleRepository rewardRuleRepository;
    private final AdminRepository adminRepository;
    
    private final Random random = new Random();
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        run();
    }
    
    public void run() {
        try {
            log.info("=== SAMPLE DATA GENERATOR TRIGGERED ===");
            
            long userCount = userRepository.count();
            log.info("Current user count: {}", userCount);
            
            // Even if users exist, ensure acceptance data exists for merchants
            try {
                seedAcceptancesForExistingMerchantsIfMissing();
            } catch (Exception e) {
                log.warn("Acceptance seeding skipped due to error: {}", e.getMessage());
            }
            // Ensure users have a default password if missing
            try {
                backfillUserPasswordsIfMissing();
            } catch (Exception e) {
                log.warn("User password backfill skipped due to error: {}", e.getMessage());
            }
            // Ensure at least one admin exists
            try {
                seedDefaultAdmin();
            } catch (Exception e) {
                log.warn("Admin seeding skipped due to error: {}", e.getMessage());
            }
            // Ensure reward programs exist
            try {
                seedRewardProgramsIfMissing();
            } catch (Exception e) {
                log.warn("Reward program seeding skipped due to error: {}", e.getMessage());
            }
            
            if (userCount > 0) {
                log.info("Database already populated with {} users. Skipping full data generation.", userCount);
                return;
            }
            
            log.info("=== Starting sample data generation ===");
            generateSampleData();
            log.info("=== Data generation completed! ===");
        } catch (Exception e) {
            log.error("Error during sample data generation", e);
        }
    }

    private void seedDefaultAdmin() {
        long count = adminRepository.count();
        if (count > 0) {
            return;
        }
        AdminNode admin = AdminNode.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password("pass123")
                .build();
        adminRepository.save(admin);
        log.info("Seeded default admin user: admin@example.com / pass123");
    }

    private void backfillUserPasswordsIfMissing() {
        List<UserNode> users = userRepository.findAll();
        if (users.isEmpty()) return;
        int updated = 0;
        for (UserNode u : users) {
            if (u.getPassword() == null || u.getPassword().isBlank()) {
                u.setPassword("pass123");
                userRepository.save(u);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Backfilled default passwords for {} existing users.", updated);
        }
    }
    
    private void generateSampleData() {
        // Step 1: Create categories
        List<CategoryNode> categories = createCategories();
        
        // Step 2: Create regions
        List<RegionNode> regions = createRegions();
        
        // Step 3: Create acceptances
        List<AcceptanceNode> acceptances = createAcceptances();
        
        // Step 4: Create merchants
        List<MerchantNode> merchants = createMerchants(categories, regions, acceptances);
        
        // Step 5: Create users and cards with transactions
        createUsersCardsAndTransactions(merchants, regions);
        
        log.info("Sample data generation completed successfully!");
    }
    
    private List<CategoryNode> createCategories() {
        List<CategoryNode> categories = new ArrayList<>();
        
        String[][] categoryData = {
            {"5411", "Grocery Stores"},
            {"5812", "Eating Places & Restaurants"},
            {"5999", "Miscellaneous Retail Stores"},
            {"7832", "Motion Pictures"},
            {"7011", "Hotels & Motels"},
            {"5541", "Service Stations"},
            {"5912", "Drug Stores & Pharmacies"},
            {"4900", "Utilities"},
            {"5732", "Electronics Stores"},
            {"5651", "Family Clothing Stores"},
            {"5200", "Home Supply Stores"},
            {"7399", "Business Services"}
        };
        
        for (String[] data : categoryData) {
            CategoryNode category = new CategoryNode();
            category.setCode(data[0]);
            category.setName(data[1]);
            categories.add(categoryRepository.save(category));
        }
        
        log.info("Created {} categories", categories.size());
        return categories;
    }
    
    private List<RegionNode> createRegions() {
        List<RegionNode> regions = new ArrayList<>();
        
        Object[][] regionData = {
            {"New York", "USA", 40.7128, -74.0060},
            {"San Francisco", "USA", 37.7749, -122.4194},
            {"Los Angeles", "USA", 34.0522, -118.2437},
            {"Chicago", "USA", 41.8781, -87.6298},
            {"London", "UK", 51.5074, -0.1278},
            {"Paris", "France", 48.8566, 2.3522},
            {"Berlin", "Germany", 52.5200, 13.4050},
            {"Tokyo", "Japan", 35.6762, 139.6503}
        };
        
        for (Object[] data : regionData) {
            RegionNode region = new RegionNode();
            region.setName((String) data[0]);
            region.setCity((String) data[0]);
            region.setCountry((String) data[1]);
            region.setLat((Double) data[2]);
            region.setLon((Double) data[3]);
            regions.add(regionRepository.save(region));
        }
        
        log.info("Created {} regions", regions.size());
        return regions;
    }
    
    private List<AcceptanceNode> createAcceptances() {
        List<AcceptanceNode> acceptances = new ArrayList<>();
        
        CardNetwork[] networks = {CardNetwork.VISA, CardNetwork.MASTERCARD, CardNetwork.AMEX, CardNetwork.DISCOVER};
        CardType[] types = {CardType.CREDIT, CardType.DEBIT};
        
        for (CardNetwork network : networks) {
            for (CardType type : types) {
                AcceptanceNode acceptance = new AcceptanceNode();
                acceptance.setNetwork(network);
                acceptance.setType(type);
                acceptances.add(acceptanceRepository.save(acceptance));
            }
        }
        
        log.info("Created {} acceptance types", acceptances.size());
        return acceptances;
    }
    
    private List<MerchantNode> createMerchants(List<CategoryNode> categories, List<RegionNode> regions, List<AcceptanceNode> acceptances) {
        List<MerchantNode> merchants = new ArrayList<>();
        
        String[][] merchantNames = {
            {"Walmart", "Target", "Costco", "Whole Foods"},
            {"Starbucks", "McDonald's", "Chipotle", "Subway"},
            {"Amazon", "eBay", "Best Buy", "Macy's"},
            {"Netflix", "AMC Theaters", "Spotify"},
            {"Hilton", "Marriott", "Booking.com"},
            {"Shell", "BP", "Exxon"},
            {"CVS", "Walgreens", "GNC"},
            {"AT&T", "Verizon", "Comcast"},
            {"Apple Store", "Microsoft Store"},
            {"Zara", "H&M", "Nike"},
            {"IKEA", "Home Depot"},
            {"Uber", "Lyft"}
        };
        
        int merchantId = 1;
        for (int catIdx = 0; catIdx < Math.min(merchantNames.length, categories.size()); catIdx++) {
            CategoryNode category = categories.get(catIdx);
            
            for (String merchantName : merchantNames[catIdx]) {
                MerchantNode merchant = new MerchantNode();
                merchant.setMerchantId("M" + String.format("%05d", merchantId++));
                merchant.setName(merchantName);
                merchant.setCategory(category);
                merchant.setRegion(regions.get(random.nextInt(regions.size())));
                // Generate a simple demo email for merchant login
        String slug = merchantName.toLowerCase()
            .replaceAll("[^a-z0-9]+", ".")
            .replaceAll("^\\.|\\.$", "");
                // Ensure unique merchant email by including external merchantId
                merchant.setEmail(slug + "." + merchant.getMerchantId().toLowerCase() + "@merchant.test");
                // Demo password for merchants
                merchant.setPassword("pass123");
                
                // Assign a random subset of acceptance combinations to each merchant
                Set<CardAcceptance> accepts = new HashSet<>();
                int minAccepts = 2;
                int maxAccepts = Math.max(minAccepts, Math.min(acceptances.size(), 4));
                int count = minAccepts + random.nextInt(maxAccepts - minAccepts + 1);

                // Shuffle a copy of the list to pick first N
                List<AcceptanceNode> shuffled = new ArrayList<>(acceptances);
                Collections.shuffle(shuffled, random);
                for (int k = 0; k < count; k++) {
                    AcceptanceNode acc = shuffled.get(k);
                    CardAcceptance ca = CardAcceptance.builder()
                            .acceptance(acc)
                            .minAmount(random.nextBoolean() ? (double) (10 * (random.nextInt(20) + 1)) : null)
                            .contactless(random.nextInt(100) < 60)
                            .installmentsAllowed(random.nextInt(100) < 30)
                            .build();
                    accepts.add(ca);
                }
                merchant.setAccepts(accepts);

                merchants.add(merchantRepository.save(merchant));
            }
        }
        
        log.info("Created {} merchants", merchants.size());
        return merchants;
    }
    
    private void createUsersCardsAndTransactions(List<MerchantNode> merchants, List<RegionNode> regions) {
        String[] firstNames = {"John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa", "James", "Mary"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        for (int i = 1; i <= 200; i++) {
            // Create user
            UserNode user = new UserNode();
            user.setExternalId("USR" + String.format("%05d", i));
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            user.setFullName(firstName + " " + lastName);
            user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@example.com");
            // Demo password for users
            user.setPassword("pass123");
            user.setAge(random.nextInt(50) + 20);
            user.setMemberSince(Instant.now().minusSeconds(random.nextInt(1095) * 86400L));
            
            RegionNode homeRegion = regions.get(random.nextInt(regions.size()));
            user.setHomeCity(homeRegion.getCity());
            user.setHomeCountry(homeRegion.getCountry());
            user.setIsFrequentTraveler(random.nextInt(100) < 20);
            
            // Income level and lifestage
            IncomeLevel[] incomeLevels = IncomeLevel.values();
            Lifestage[] lifestages = Lifestage.values();
            user.setIncomeLevel(incomeLevels[random.nextInt(incomeLevels.length)]);
            user.setLifestage(lifestages[random.nextInt(lifestages.length)]);
            
            user = userRepository.save(user);
            
            // Create 1-3 cards per user
            int cardCount = random.nextInt(3) + 1;
            for (int j = 0; j < cardCount; j++) {
                createCardWithTransactions(user, j + 1, merchants);
            }
        }
        
        log.info("Created 200 users with cards and transactions");
    }

    private void seedRewardProgramsIfMissing() {
        long existingPrograms = rewardProgramRepository.count();
        if (existingPrograms > 0) {
            log.info("Reward programs already present ({}). Skipping seeding.", existingPrograms);
            return;
        }

        List<CardNode> allCards = cardRepository.findAll();
        if (allCards.isEmpty()) {
            log.info("No cards present; reward program seeding skipped.");
            return;
        }

        // Build 2 simple programs with rules for common categories
        RewardProgram progA = RewardProgram.builder()
                .issuerBank("Bank A")
                .name("Bank A Everyday Rewards")
                .startDate(Instant.now().minusSeconds(60L * 60 * 24 * 180))
                .endDate(null)
                .build();
        Set<CardNode> progACards = new java.util.HashSet<>(allCards.subList(0, Math.min(5, allCards.size())));
        progA.setCards(progACards);
        Set<RewardRule> aRules = new java.util.HashSet<>();
        aRules.add(RewardRule.builder().categoryCode("5411").rewardRate(0.03).cap(100.0).conditions("Grocery 3% cashback up to $100/mo").build());
        aRules.add(RewardRule.builder().categoryCode("5812").rewardRate(0.02).cap(100.0).conditions("Dining 2% cashback").build());
        progA.setRules(aRules);

        RewardProgram progB = RewardProgram.builder()
                .issuerBank("Bank B")
                .name("Traveler Perks")
                .startDate(Instant.now().minusSeconds(60L * 60 * 24 * 90))
                .endDate(null)
                .build();
        Set<CardNode> progBCards = new java.util.HashSet<>(allCards.subList(Math.min(5, allCards.size()), Math.min(10, allCards.size())));
        progB.setCards(progBCards);
        Set<RewardRule> bRules = new java.util.HashSet<>();
        bRules.add(RewardRule.builder().categoryCode("7011").rewardRate(0.05).cap(200.0).conditions("Hotels 5% cashback").build());
        bRules.add(RewardRule.builder().categoryCode("5732").rewardRate(0.02).cap(100.0).conditions("Electronics 2% cashback").build());
        progB.setRules(bRules);

        rewardProgramRepository.save(progA);
        rewardProgramRepository.save(progB);

        log.info("Seeded {} reward programs with sample rules.", rewardProgramRepository.count());
    }
    
    private CardNode createCardWithTransactions(UserNode user, int cardNum, List<MerchantNode> merchants) {
        CardNode card = new CardNode();
        card.setPanHash("CARD" + user.getExternalId() + String.format("%02d", cardNum));
        card.setNetwork(random.nextBoolean() ? CardNetwork.VISA : CardNetwork.MASTERCARD);
        card.setType(random.nextBoolean() ? CardType.CREDIT : CardType.DEBIT);
        card.setIssuerCountry(user.getHomeCountry());
        card.setMonthlyLimit((double) (random.nextInt(10) + 1) * 1000);
        card.setOwner(user);
        
        // Create 20-40 transactions per card
        int txnCount = random.nextInt(21) + 20;
        List<TransactionRel> transactions = new ArrayList<>();
        
        for (int i = 0; i < txnCount; i++) {
            // Random date between Jan 2024 and Nov 2025
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 11, 1);
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
            LocalDate randomDate = startDate.plusDays(random.nextLong(daysBetween));
            
            int hour = random.nextInt(24);
            int minute = random.nextInt(60);
            Instant timestamp = randomDate.atTime(hour, minute).toInstant(ZoneOffset.UTC);
            
            MerchantNode merchant = merchants.get(random.nextInt(merchants.size()));
            
            TransactionRel transaction = new TransactionRel();
            transaction.setAmount(Math.round((random.nextDouble() * 400 + 10) * 100.0) / 100.0);
            transaction.setCurrency(rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.Currency.USD);
            transaction.setTimestamp(timestamp);
            
            // 95% success rate
            transaction.setStatus(random.nextInt(100) < 95 ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
            
            Channel[] channels = Channel.values();
            transaction.setChannel(channels[random.nextInt(channels.length)]);
            
            PaymentPurpose[] purposes = PaymentPurpose.values();
            transaction.setPurpose(purposes[random.nextInt(purposes.length)]);
            
            PaymentType[] paymentTypes = PaymentType.values();
            transaction.setPaymentType(paymentTypes[random.nextInt(paymentTypes.length)]);
            
            transaction.setContactless(random.nextInt(100) < 40);
            
            if (transaction.getStatus() != TransactionStatus.SUCCESS) {
                DeclineReason[] reasons = DeclineReason.values();
                transaction.setDeclineReason(reasons[random.nextInt(reasons.length)]);
            }
            
            transaction.setMerchant(merchant);
            transactions.add(transaction);
        }
        
        card.setTransactions(new HashSet<>(transactions));
        return cardRepository.save(card);
    }

    // Seed ACCEPTS relationships for existing merchants if missing
    private void seedAcceptancesForExistingMerchantsIfMissing() {
        List<MerchantNode> allMerchants = merchantRepository.findAll();
        if (allMerchants.isEmpty()) {
                log.info("No merchants present yet; acceptance seeding will be handled by full generation.");
                return;
            }

        // Ensure we have Acceptance nodes
        List<AcceptanceNode> acceptances = acceptanceRepository.findAll();
        if (acceptances.isEmpty()) {
            log.info("No Acceptance nodes found; creating defaults...");
            acceptances = createAcceptances();
        }

    int updated = 0;
        // Track seen emails to detect duplicates
        Set<String> seenEmails = new HashSet<>();
        for (MerchantNode m : allMerchants) {
                // Backfill merchant email for login; ensure uniqueness across nodes
                String slug = m.getName() == null ? null : m.getName().toLowerCase()
                        .replaceAll("[^a-z0-9]+", ".")
                        .replaceAll("^\\.|\\.$", "");
                String uniqueEmail = (slug == null || slug.isBlank()) ? null
                        : slug + "." + (m.getMerchantId() == null ? (m.getId() == null ? "m" + Math.abs(new Random().nextInt()) : ("id" + m.getId())) : m.getMerchantId().toLowerCase()) + "@merchant.test";

                if (m.getEmail() == null || m.getEmail().isBlank()) {
                    if (uniqueEmail != null) {
                        m.setEmail(uniqueEmail);
                    }
                } else {
                    // If duplicate encountered, override with unique pattern
                    if (seenEmails.contains(m.getEmail()) && uniqueEmail != null) {
                        m.setEmail(uniqueEmail);
                    }
                }
                if (m.getEmail() != null) {
                    seenEmails.add(m.getEmail());
                }
                // Backfill default password if missing
                if (m.getPassword() == null || m.getPassword().isBlank()) {
                    m.setPassword("pass123");
                }

                if (m.getAccepts() == null || m.getAccepts().isEmpty()) {
                Set<CardAcceptance> accepts = new HashSet<>();
                int minAccepts = 2;
                int maxAccepts = Math.max(minAccepts, Math.min(acceptances.size(), 4));
                int count = minAccepts + random.nextInt(maxAccepts - minAccepts + 1);

                List<AcceptanceNode> shuffled = new ArrayList<>(acceptances);
                Collections.shuffle(shuffled, random);
                for (int i = 0; i < count; i++) {
                    AcceptanceNode acc = shuffled.get(i);
                    CardAcceptance ca = CardAcceptance.builder()
                            .acceptance(acc)
                            .minAmount(random.nextBoolean() ? (double) (10 * (random.nextInt(20) + 1)) : null)
                            .contactless(random.nextInt(100) < 60)
                            .installmentsAllowed(random.nextInt(100) < 30)
                            .build();
                    accepts.add(ca);
                }
                m.setAccepts(accepts);
                }

                // Save if we changed anything (email or accepts)
                if ((m.getEmail() != null && !m.getEmail().isBlank()) || (m.getAccepts() != null && !m.getAccepts().isEmpty())) {
                    merchantRepository.save(m);
                    updated++;
            }
        }
        if (updated > 0) {
                log.info("Backfilled email and/or acceptance relationships for {} existing merchants.", updated);
        } else {
            log.info("Acceptance relationships already present for existing merchants; no seeding needed.");
        }
    }
    
    /**
     * Add sample transactions for a specific user by email
     */
    public void addTransactionsForUser(String email, int cardCount, int transactionsPerCard) {
        Optional<UserNode> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        UserNode user = userOpt.get();
        List<MerchantNode> merchants = merchantRepository.findAll();
        if (merchants.isEmpty()) {
            throw new RuntimeException("No merchants found in database. Cannot create transactions.");
        }
        
        log.info("Adding {} cards with {} transactions each for user: {}", cardCount, transactionsPerCard, email);
        
        for (int i = 0; i < cardCount; i++) {
            CardNode card = new CardNode();
            card.setPanHash("CARD" + user.getExternalId() + String.format("%02d", i + 1));
            card.setNetwork(random.nextBoolean() ? CardNetwork.VISA : CardNetwork.MASTERCARD);
            card.setType(random.nextBoolean() ? CardType.CREDIT : CardType.DEBIT);
            card.setIssuerCountry(user.getHomeCountry());
            card.setMonthlyLimit((double) (random.nextInt(10) + 1) * 1000);
            card.setOwner(user);
            
            List<TransactionRel> transactions = new ArrayList<>();
            
            for (int j = 0; j < transactionsPerCard; j++) {
                // Random date between Jan 2024 and Nov 2025
                LocalDate startDate = LocalDate.of(2024, 1, 1);
                LocalDate endDate = LocalDate.of(2025, 11, 1);
                long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
                LocalDate randomDate = startDate.plusDays(random.nextLong(daysBetween));
                
                int hour = random.nextInt(24);
                int minute = random.nextInt(60);
                Instant timestamp = randomDate.atTime(hour, minute).toInstant(ZoneOffset.UTC);
                
                MerchantNode merchant = merchants.get(random.nextInt(merchants.size()));
                
                TransactionRel transaction = new TransactionRel();
                transaction.setAmount(Math.round((random.nextDouble() * 400 + 10) * 100.0) / 100.0);
                transaction.setCurrency(rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.Currency.USD);
                transaction.setTimestamp(timestamp);
                transaction.setStatus(random.nextInt(100) < 95 ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
                
                Channel[] channels = Channel.values();
                transaction.setChannel(channels[random.nextInt(channels.length)]);
                
                PaymentPurpose[] purposes = PaymentPurpose.values();
                transaction.setPurpose(purposes[random.nextInt(purposes.length)]);
                
                PaymentType[] paymentTypes = PaymentType.values();
                transaction.setPaymentType(paymentTypes[random.nextInt(paymentTypes.length)]);
                
                transaction.setContactless(random.nextInt(100) < 40);
                
                transaction.setMerchant(merchant);
                transactions.add(transaction);
            }
            
            card.setTransactions(new HashSet<>(transactions));
            cardRepository.save(card);
        }
        
        log.info("Successfully added {} cards with {} transactions each for user: {}", cardCount, transactionsPerCard, email);
    }
}
