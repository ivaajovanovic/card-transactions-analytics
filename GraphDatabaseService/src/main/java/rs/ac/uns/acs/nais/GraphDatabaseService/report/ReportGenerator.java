package rs.ac.uns.acs.nais.GraphDatabaseService.report;

import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IUserService;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IMerchantService;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Merchant;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportGenerator {
    
    private final AnalyticsService analyticsService;
    private final IUserService userService;
    private final IMerchantService merchantService;
    
    /**
     * Generiše kompletan PDF izveštaj koji sadrži:
     * - Proste sekcije: Lista korisnika, Lista trgovaca
     * - Složene sekcije: Analitike (top trgovci, sumnjive POS transakcije, cross-channel aktivnost)
     */
    public byte[] generateCompleteAnalyticsReport(String userId) {
        System.out.println("Starting to generate Card Transactions Analytics Report...");
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            // Naslov izveštaja
            addTitle(document);
            
            // PROSTA SEKCIJA 1: Lista svih korisnika
            addSimpleUsersSection(document);
            
            // PROSTA SEKCIJA 2: Lista svih trgovaca
            addSimpleMerchantsSection(document);
            
            // SLOŽENA SEKCIJA 1: Top trgovci za korisnika
            if (userId != null && !userId.trim().isEmpty()) {
                addComplexTopMerchantsSection(document, userId);
                
                // SLOŽENA SEKCIJA 2: Analiza potrošnje po kategorijama
                addComplexCategorySpendingSection(document, userId);
            }
            
            // SLOŽENA SEKCIJA 3: Sumnjive POS transakcije (bez korisnika)
            addComplexSuspiciousPosSection(document);
            
            // SLOŽENA SEKCIJA 4: Cross-channel aktivnost
            addComplexCrossChannelSection(document);
            
            System.out.println("Card Transactions Analytics Report generated successfully!");
            
        } catch (DocumentException e) {
            System.out.println("Error: Document exception: " + e.getMessage());
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }
    
    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Card Transactions Analytics Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph subtitle = new Paragraph("Generated on: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        
        document.add(new Paragraph(" ")); // Razmak
    }
    
    /**
     * PROSTA SEKCIJA 1: Prikaz svih korisnika u sistemu
     */
    private void addSimpleUsersSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("1. Korisnici u sistemu (Prosta sekcija)", sectionFont);
        document.add(sectionTitle);
        document.add(new Paragraph(" "));
        
        List<User> users = userService.list();
        
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "User ID", "Name", "Email");
        
        // Data
        for (User user : users) {
            table.addCell(user.getId());
            table.addCell(user.getName());
            table.addCell(user.getEmail() != null ? user.getEmail() : "N/A");
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
    
    /**
     * PROSTA SEKCIJA 2: Prikaz svih trgovaca u sistemu
     */
    private void addSimpleMerchantsSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("2. Trgovci u sistemu (Prosta sekcija)", sectionFont);
        document.add(sectionTitle);
        document.add(new Paragraph(" "));
        
        List<Merchant> merchants = merchantService.list();
        
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "Merchant ID", "Name", "Location");
        
        // Data
        for (Merchant merchant : merchants) {
            table.addCell(merchant.getId());
            table.addCell(merchant.getName());
            table.addCell(merchant.getLocation() != null ? merchant.getLocation() : "N/A");
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
    
    /**
     * SLOŽENA SEKCIJA 1: Analiza top trgovaca za korisnika
     */
    private void addComplexTopMerchantsSection(Document document, String userId) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("3. Top trgovci za korisnika (Složena sekcija)", sectionFont);
        document.add(sectionTitle);
        document.add(new Paragraph(" "));
        
        List<TopMerchantView> topMerchants = analyticsService.getTopMerchantsByUser(userId, 10);
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "Merchant", "Tx Count", "POS Amount", "Online Amount", "Total", "Dominant Channel");
        
        // Data
        for (TopMerchantView merchant : topMerchants) {
            table.addCell(merchant.merchant());
            table.addCell(merchant.txCount().toString());
            table.addCell(String.format("%.2f", merchant.posAmt()));
            table.addCell(String.format("%.2f", merchant.onlineAmt()));
            table.addCell(String.format("%.2f", merchant.total()));
            
            String dominantChannel = merchant.posAmt() > merchant.onlineAmt() ? "POS" : "ONLINE";
            table.addCell(dominantChannel);
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
    
    /**
     * SLOŽENA SEKCIJA 2: Analiza potrošnje po kategorijama po danima
     */
    private void addComplexCategorySpendingSection(Document document, String userId) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("4. Potrošnja po kategorijama (poslednih 30 dana) (Složena sekcija)", sectionFont);
        document.add(sectionTitle);
        document.add(new Paragraph(" "));
        
        List<CategorySpendPoint> categorySpending = analyticsService.getCategorySpendByDay(userId, 30);
        
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "Category", "Date", "Daily Amount");
        
        // Data
        for (CategorySpendPoint point : categorySpending) {
            table.addCell(point.category());
            table.addCell(point.date().toString());
            table.addCell(String.format("%.2f", point.daily()));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
    
    /**
     * SLOŽENA SEKCIJA 3: Sumnjive POS transakcije (bez prisutnosti kartice)
     */
    private void addComplexSuspiciousPosSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("5. Sumnjive POS transakcije (Složena sekcija)", sectionFont);
        document.add(sectionTitle);
        
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph description = new Paragraph("POS transakcije gde kartica nije bila fizički prisutna", descFont);
        document.add(description);
        document.add(new Paragraph(" "));
        
        List<SuspiciousPosView> suspiciousPos = analyticsService.getSuspiciousPos();
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "Card ID", "Merchant", "Transaction Count", "Total Amount");
        
        // Data
        for (SuspiciousPosView suspicious : suspiciousPos) {
            table.addCell(suspicious.cardId());
            table.addCell(suspicious.merchant());
            table.addCell(suspicious.cnt().toString());
            table.addCell(String.format("%.2f", suspicious.total()));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
    
    /**
     * SLOŽENA SEKCIJA 4: Cross-channel aktivnost (POS + Online u roku od 7 dana)
     */
    private void addComplexCrossChannelSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("6. Cross-channel aktivnost (Složena sekcija)", sectionFont);
        document.add(sectionTitle);
        
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph description = new Paragraph("Korisnici koji su koristili i POS i Online kanale kod istog trgovca u roku od 7 dana", descFont);
        document.add(description);
        document.add(new Paragraph(" "));
        
        List<CrossChannelHit> crossChannelHits = analyticsService.getCrossChannelWithin7d();
        
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        
        // Header
        addTableHeader(table, headerFont, "User", "Merchant", "Cross-channel Occurrences");
        
        // Data
        for (CrossChannelHit hit : crossChannelHits) {
            table.addCell(hit.user());
            table.addCell(hit.merchant());
            table.addCell(hit.occurrences().toString());
        }
        
        document.add(table);
    }
    
    /**
     * Helper metoda za dodavanje header-a u tabelu
     */
    private void addTableHeader(PdfPTable table, Font headerFont, String... headers) {
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
        }
    }
}