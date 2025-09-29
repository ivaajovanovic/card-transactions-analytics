package rs.ac.uns.acs.nais.columnar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.columnar.dto.ReportRequestDTO;
import rs.ac.uns.acs.nais.columnar.dto.ReportResponseDTO;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servis za generisanje PDF izveštaja
 * Implementira zahteve za export u PDF format pomoću Grafana ili drugih alata
 */
@Service
@Slf4j
public class PDFGeneratorService {
    
    /**
     * Generiše PDF za jednostavan izveštaj (top proizvodi)
     */
    public byte[] generateSimpleReportPDF(List<ReportResponseDTO.ProductSummary> topProducts, ReportRequestDTO request) {
        log.info("=== PDF GENERATOR === Generating simple report PDF");
        
        try {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>\n");
            htmlContent.append("<html>\n<head>\n");
            htmlContent.append("<meta charset='UTF-8'>\n");
            htmlContent.append("<title>Top Products Report</title>\n");
            htmlContent.append("<style>\n");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            htmlContent.append("table { border-collapse: collapse; width: 100%; }\n");
            htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            htmlContent.append("th { background-color: #f2f2f2; }\n");
            htmlContent.append("h1 { color: #333; }\n");
            htmlContent.append("</style>\n");
            htmlContent.append("</head>\n<body>\n");
            
            // Header
            htmlContent.append("<h1>Top Products Report</h1>\n");
            htmlContent.append("<p><strong>Period:</strong> ")
                      .append(request.getStartDate())
                      .append(" to ")
                      .append(request.getEndDate())
                      .append("</p>\n");
            htmlContent.append("<p><strong>Generated:</strong> ")
                      .append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                      .append("</p>\n");
            
            // Table
            htmlContent.append("<table>\n");
            htmlContent.append("<tr>\n");
            htmlContent.append("<th>Category ID</th>\n");
            htmlContent.append("<th>Category Name</th>\n");
            htmlContent.append("<th>Transaction Count</th>\n");
            htmlContent.append("<th>Total Amount (EUR)</th>\n");
            htmlContent.append("<th>Average Amount (EUR)</th>\n");
            htmlContent.append("</tr>\n");
            
            for (ReportResponseDTO.ProductSummary product : topProducts) {
                htmlContent.append("<tr>\n");
                htmlContent.append("<td>").append(product.getCategoryId()).append("</td>\n");
                htmlContent.append("<td>").append(product.getCategoryName()).append("</td>\n");
                htmlContent.append("<td>").append(product.getTransactionCount()).append("</td>\n");
                htmlContent.append("<td>").append(String.format("%.2f", product.getTotalAmount())).append("</td>\n");
                htmlContent.append("<td>").append(String.format("%.2f", product.getAverageAmount())).append("</td>\n");
                htmlContent.append("</tr>\n");
            }
            
            htmlContent.append("</table>\n");
            htmlContent.append("</body>\n</html>");
            
            // Za sada vraćamo HTML kao byte array
            // U production verziji bi se koristio iText PDF ili slični alat
            return htmlContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("=== PDF GENERATOR === Error generating simple report PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
    
    /**
     * Generiše PDF za složen analitički izveštaj
     */
    public byte[] generateComplexReportPDF(List<ReportResponseDTO.TransactionSummary> transactions, 
                                         Map<String, Object> aggregatedData, 
                                         ReportRequestDTO request) {
        log.info("=== PDF GENERATOR === Generating complex report PDF");
        
        try {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>\n");
            htmlContent.append("<html>\n<head>\n");
            htmlContent.append("<meta charset='UTF-8'>\n");
            htmlContent.append("<title>Complex Analytics Report</title>\n");
            htmlContent.append("<style>\n");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            htmlContent.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n");
            htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            htmlContent.append("th { background-color: #f2f2f2; }\n");
            htmlContent.append("h1, h2 { color: #333; }\n");
            htmlContent.append(".summary { background-color: #f9f9f9; padding: 15px; margin-bottom: 20px; }\n");
            htmlContent.append("</style>\n");
            htmlContent.append("</head>\n<body>\n");
            
            // Header
            htmlContent.append("<h1>Complex Analytics Report</h1>\n");
            htmlContent.append("<p><strong>Period:</strong> ")
                      .append(request.getStartDate())
                      .append(" to ")
                      .append(request.getEndDate())
                      .append("</p>\n");
            htmlContent.append("<p><strong>Generated:</strong> ")
                      .append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                      .append("</p>\n");
            
            // Summary section
            htmlContent.append("<div class='summary'>\n");
            htmlContent.append("<h2>Summary Statistics</h2>\n");
            htmlContent.append("<p><strong>Total Amount:</strong> ")
                      .append(String.format("%.2f EUR", (Double) aggregatedData.getOrDefault("totalAmount", 0.0)))
                      .append("</p>\n");
            htmlContent.append("<p><strong>Average Amount:</strong> ")
                      .append(String.format("%.2f EUR", (Double) aggregatedData.getOrDefault("averageAmount", 0.0)))
                      .append("</p>\n");
            htmlContent.append("<p><strong>Transaction Count:</strong> ")
                      .append(aggregatedData.getOrDefault("transactionCount", 0))
                      .append("</p>\n");
            htmlContent.append("</div>\n");
            
            // Transactions table (limited to first 50 for PDF)
            htmlContent.append("<h2>Transaction Details</h2>\n");
            htmlContent.append("<table>\n");
            htmlContent.append("<tr>\n");
            htmlContent.append("<th>Transaction ID</th>\n");
            htmlContent.append("<th>User ID</th>\n");
            htmlContent.append("<th>Merchant ID</th>\n");
            htmlContent.append("<th>Category ID</th>\n");
            htmlContent.append("<th>Amount</th>\n");
            htmlContent.append("<th>Currency</th>\n");
            htmlContent.append("<th>Status</th>\n");
            htmlContent.append("</tr>\n");
            
            int limit = Math.min(50, transactions.size()); // Limit for PDF
            for (int i = 0; i < limit; i++) {
                ReportResponseDTO.TransactionSummary tx = transactions.get(i);
                htmlContent.append("<tr>\n");
                htmlContent.append("<td>").append(tx.getTxId()).append("</td>\n");
                htmlContent.append("<td>").append(tx.getUserId().substring(0, 8)).append("...</td>\n");
                htmlContent.append("<td>").append(tx.getMerchantId()).append("</td>\n");
                htmlContent.append("<td>").append(tx.getCategoryId()).append("</td>\n");
                htmlContent.append("<td>").append(String.format("%.2f", tx.getAmount())).append("</td>\n");
                htmlContent.append("<td>").append(tx.getCurrency()).append("</td>\n");
                htmlContent.append("<td>").append(tx.getStatus()).append("</td>\n");
                htmlContent.append("</tr>\n");
            }
            
            if (transactions.size() > 50) {
                htmlContent.append("<tr><td colspan='7'><em>... and ")
                          .append(transactions.size() - 50)
                          .append(" more transactions</em></td></tr>\n");
            }
            
            htmlContent.append("</table>\n");
            htmlContent.append("</body>\n</html>");
            
            // Za sada vraćamo HTML kao byte array
            return htmlContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("=== PDF GENERATOR === Error generating complex report PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
    
    /**
     * Generiše PDF sa grafikonima (placeholder za Grafana integraciju)
     */
    public byte[] generateGraphicalReportPDF(List<ReportResponseDTO.ChartData> chartData, ReportRequestDTO request) {
        log.info("=== PDF GENERATOR === Generating graphical report PDF");
        
        try {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>\n");
            htmlContent.append("<html>\n<head>\n");
            htmlContent.append("<meta charset='UTF-8'>\n");
            htmlContent.append("<title>Graphical Report</title>\n");
            htmlContent.append("<style>\n");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            htmlContent.append("h1, h2 { color: #333; }\n");
            htmlContent.append(".chart-placeholder { border: 2px dashed #ccc; padding: 50px; text-align: center; margin: 20px 0; }\n");
            htmlContent.append("</style>\n");
            htmlContent.append("</head>\n<body>\n");
            
            htmlContent.append("<h1>Graphical Report</h1>\n");
            htmlContent.append("<p><strong>Theme:</strong> ").append(request.getTheme()).append("</p>\n");
            htmlContent.append("<p><strong>Generated:</strong> ")
                      .append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                      .append("</p>\n");
            
            htmlContent.append("<h2>Chart Data</h2>\n");
            htmlContent.append("<div class='chart-placeholder'>\n");
            htmlContent.append("[GRAFANA CHART PLACEHOLDER]\n");
            htmlContent.append("<br>Chart would be integrated here using Grafana API\n");
            htmlContent.append("<br>Data points: ").append(chartData.size()).append("\n");
            htmlContent.append("</div>\n");
            
            // Data table
            htmlContent.append("<h2>Data Points</h2>\n");
            htmlContent.append("<table>\n");
            htmlContent.append("<tr><th>Label</th><th>Value</th><th>Category</th></tr>\n");
            
            for (ReportResponseDTO.ChartData data : chartData) {
                htmlContent.append("<tr>\n");
                htmlContent.append("<td>").append(data.getLabel()).append("</td>\n");
                htmlContent.append("<td>").append(String.format("%.2f", data.getValue())).append("</td>\n");
                htmlContent.append("<td>").append(data.getCategory()).append("</td>\n");
                htmlContent.append("</tr>\n");
            }
            
            htmlContent.append("</table>\n");
            htmlContent.append("</body>\n</html>");
            
            return htmlContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("=== PDF GENERATOR === Error generating graphical report PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}