package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.report;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.AnalyticsService;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerator {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Generates a complete PDF report with general analytics (all users)
     */
    public byte[] generateCompleteAnalyticsReport() {
        log.info("Starting to generate General Transaction Analytics Report...");
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            // Report Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("General Transaction Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            // ==================== SIMPLE SECTIONS ====================
            
            // Simple Section 1: Recent Transactions
            addRecentTransactionsSection(document);
            
            // Simple Section 2: Weekly Spending Totals
            addWeeklySpendingTotalsSection(document);
            
            // ==================== COMPLEX SECTION ====================
            
            // Complex Section: Top 10 Keywords by Spending
            addTopKeywordsBySpendingSection(document);
            
            log.info("General Analytics Report generated successfully!");
            
        } catch (DocumentException e) {
            log.error("Error: Document exception: {}", e.getMessage());
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }
    
    // Helper method for adding table headers
    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    // Helper method for adding stats rows
    private void addStatsRow(PdfPTable table, Font font, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    // ==================== USER-SPECIFIC REPORT SECTIONS ====================

    /**
     * SIMPLE SECTION 1: Recent Transactions
     * Fetch the last 20 transactions sorted by date descending
     */
    private void addRecentTransactionsSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("1. Recent Transactions (Last 20)", sectionFont);
        document.add(sectionTitle);
        
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph description = new Paragraph("All transactions sorted by date descending", descFont);
        document.add(description);
        document.add(new Paragraph(" "));
        
        List<Map<String, Object>> recentTransactions = analyticsService.getRecentTransactions();
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        addTableHeader(table, headerFont, "Date", "Card ID", "Merchant ID", "Amount", "Description");
        
        for (Map<String, Object> transaction : recentTransactions) {
            table.addCell((String) transaction.get("date"));
            table.addCell((String) transaction.get("cardId"));
            table.addCell((String) transaction.get("merchantId"));
            table.addCell(String.format("$%.2f", (Double) transaction.get("amount")));
            table.addCell((String) transaction.get("description"));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }

    /**
     * SIMPLE SECTION 2: Weekly Spending Totals (Last 3 Months)
     * Aggregate by calendar week
     */
    private void addWeeklySpendingTotalsSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("2. Weekly Spending Totals (Last 3 Months)", sectionFont);
        document.add(sectionTitle);
        
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph description = new Paragraph("All transactions in last 3 months, aggregated by calendar week", descFont);
        document.add(description);
        document.add(new Paragraph(" "));
        
        List<Map<String, Object>> weeklyTotals = analyticsService.getWeeklySpendingTotals();
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        addTableHeader(table, headerFont, "Week (Monday)", "Total Spent");
        
        for (Map<String, Object> week : weeklyTotals) {
            table.addCell((String) week.get("week"));
            table.addCell(String.format("$%.2f", (Double) week.get("totalSpent")));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }

    /**
     * COMPLEX SECTION: Top 10 Keywords by Spending (Last 3 Months)
     * Aggregation: terms aggregation on description, sum(amount) per keyword
     */
    private void addTopKeywordsBySpendingSection(Document document) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph("3. Top 10 Keywords by Spending (Last 3 Months)", sectionFont);
        document.add(sectionTitle);
        
        Font descFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph description = new Paragraph("All transactions in last 3 months, terms aggregation on description", descFont);
        document.add(description);
        document.add(new Paragraph(" "));
        
        List<Map<String, Object>> topKeywords = analyticsService.getTopKeywordsBySpending();
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        addTableHeader(table, headerFont, "Keyword", "Total Spent");
        
        for (Map<String, Object> keyword : topKeywords) {
            table.addCell((String) keyword.get("keyword"));
            table.addCell(String.format("$%.2f", (Double) keyword.get("totalSpent")));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
    }
}