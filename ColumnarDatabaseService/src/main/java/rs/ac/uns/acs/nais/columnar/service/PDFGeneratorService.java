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
                htmlContent.append("<td>").append(tx.getTransactionId()).append("</td>\n");
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
    
    /**
     * Generiše PDF za enhanced analytics izveštaj sa 4 sekcije
     */
    public byte[] generateEnhancedAnalyticsPDF(Map<String, Object> analyticsData) {
        log.info("=== PDF GENERATOR === Generating enhanced analytics PDF with 4 sections");
        
        try {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>\n");
            htmlContent.append("<html>\n<head>\n");
            htmlContent.append("<meta charset='UTF-8'>\n");
            htmlContent.append("<title>Enhanced Transaction Analytics Report</title>\n");
            htmlContent.append("<style>\n");
            htmlContent.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; color: #333; }\n");
            htmlContent.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
            htmlContent.append("h2 { color: #34495e; border-left: 4px solid #3498db; padding-left: 15px; margin-top: 30px; }\n");
            htmlContent.append("h3 { color: #7f8c8d; }\n");
            htmlContent.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n");
            htmlContent.append("th, td { border: 1px solid #bdc3c7; padding: 10px; text-align: left; }\n");
            htmlContent.append("th { background-color: #ecf0f1; font-weight: bold; }\n");
            htmlContent.append(".section { background-color: #f8f9fa; padding: 20px; margin-bottom: 25px; border-radius: 8px; }\n");
            htmlContent.append(".stat-box { display: inline-block; background: #fff; padding: 15px; margin: 10px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            htmlContent.append(".metric { font-size: 24px; font-weight: bold; color: #e74c3c; }\n");
            htmlContent.append(".currency { color: #27ae60; }\n");
            htmlContent.append(".header-info { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; margin-bottom: 30px; }\n");
            htmlContent.append("</style>\n");
            htmlContent.append("</head>\n<body>\n");
            
            // Header
            htmlContent.append("<div class='header-info'>\n");
            htmlContent.append("<h1 style='color: white; border-bottom: none; margin: 0;'>🏦 Enhanced Transaction Analytics Report</h1>\n");
            htmlContent.append("<p style='margin: 10px 0 0 0; font-size: 18px;'><strong>Period:</strong> ")
                      .append(analyticsData.get("analysis_period"))
                      .append("</p>\n");
            htmlContent.append("<p style='margin: 5px 0 0 0;'><strong>Generated:</strong> ")
                      .append(analyticsData.get("analysis_timestamp"))
                      .append("</p>\n");
            htmlContent.append("</div>\n");
            
            // BASIC STATISTICS SECTION
            Map<String, Object> basicStats = (Map<String, Object>) analyticsData.get("basic_statistics");
            if (basicStats != null) {
                htmlContent.append("<div class='section'>\n");
                htmlContent.append("<h2>📊 Basic Statistics</h2>\n");
                htmlContent.append("<div style='display: flex; flex-wrap: wrap;'>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(basicStats.get("total_transactions_in_system")).append("</div>\n");
                htmlContent.append("<div>Total Transactions</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric currency'>").append(String.format("%.2f RSD", ((Number) basicStats.get("total_amount_in_system_rsd")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Total Amount</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(basicStats.get("unique_users_total")).append("</div>\n");
                htmlContent.append("<div>Unique Users</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(basicStats.get("active_days_total")).append("</div>\n");
                htmlContent.append("<div>Active Days</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<p><strong>Transaction Period:</strong> ").append(basicStats.get("oldest_transaction_date")).append(" to ").append(basicStats.get("newest_transaction_date")).append("</p>\n");
                htmlContent.append("</div>\n");
            }
            
            // CURRENT PERIOD SECTION
            Map<String, Object> currentPeriod = (Map<String, Object>) analyticsData.get("current_period");
            if (currentPeriod != null) {
                htmlContent.append("<div class='section'>\n");
                htmlContent.append("<h2>📈 Current Period Analysis</h2>\n");
                htmlContent.append("<p><strong>Analysis Type:</strong> ").append(currentPeriod.get("analysis_type")).append("</p>\n");
                htmlContent.append("<div style='display: flex; flex-wrap: wrap;'>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(currentPeriod.get("total_transactions_analyzed")).append("</div>\n");
                htmlContent.append("<div>Transactions Analyzed</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric currency'>").append(String.format("%.2f RSD", ((Number) currentPeriod.get("total_amount_analyzed_rsd")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Total Amount</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.2f", ((Number) currentPeriod.get("average_transaction_value")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Avg Transaction (RSD)</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.2f", ((Number) currentPeriod.get("average_transactions_per_day")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Avg Transactions/Day</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("</div>\n");
                
                // Top users table
                List<Map<String, Object>> topUsers = (List<Map<String, Object>>) currentPeriod.get("top_users_by_activity");
                if (topUsers != null && !topUsers.isEmpty()) {
                    htmlContent.append("<h3>🏆 Top Users by Activity</h3>\n");
                    htmlContent.append("<table>\n");
                    htmlContent.append("<tr>\n");
                    htmlContent.append("<th>User ID</th>\n");
                    htmlContent.append("<th>Total Transactions</th>\n");
                    htmlContent.append("<th>Total Amount (RSD)</th>\n");
                    htmlContent.append("<th>Avg Transaction (RSD)</th>\n");
                    htmlContent.append("<th>Active Days</th>\n");
                    htmlContent.append("<th>Activity Intensity</th>\n");
                    htmlContent.append("</tr>\n");
                    
                    int userLimit = Math.min(10, topUsers.size());
                    for (int i = 0; i < userLimit; i++) {
                        Map<String, Object> user = topUsers.get(i);
                        htmlContent.append("<tr>\n");
                        htmlContent.append("<td>").append(user.get("user_id").toString().substring(0, 8)).append("...</td>\n");
                        htmlContent.append("<td>").append(user.get("total_transactions")).append("</td>\n");
                        htmlContent.append("<td class='currency'>").append(String.format("%.2f", ((Number) user.get("total_amount_rsd")).doubleValue())).append("</td>\n");
                        htmlContent.append("<td>").append(String.format("%.2f", ((Number) user.get("avg_transaction_value")).doubleValue())).append("</td>\n");
                        htmlContent.append("<td>").append(user.get("active_days")).append("</td>\n");
                        htmlContent.append("<td>").append(String.format("%.2f", ((Number) user.get("activity_intensity")).doubleValue())).append("</td>\n");
                        htmlContent.append("</tr>\n");
                    }
                    htmlContent.append("</table>\n");
                }
                htmlContent.append("</div>\n");
            }
            
            // PERIOD COMPARISON SECTION
            Map<String, Object> periodComparison = (Map<String, Object>) analyticsData.get("period_comparison");
            if (periodComparison != null) {
                htmlContent.append("<div class='section'>\n");
                htmlContent.append("<h2>🔄 Period Comparison</h2>\n");
                htmlContent.append("<div style='display: flex; flex-wrap: wrap;'>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.1f%%", ((Number) periodComparison.get("current_period_percentage_of_total")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Current Period % of Total</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.1f%%", ((Number) periodComparison.get("current_period_tx_percentage")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Current Period Tx %</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.2f", ((Number) periodComparison.get("period_vs_historical_activity_ratio")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Activity Ratio</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(periodComparison.get("is_current_period_above_average")).append("</div>\n");
                htmlContent.append("<div>Above Average</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("</div>\n");
            }
            
            // HISTORICAL OVERVIEW SECTION
            Map<String, Object> historicalOverview = (Map<String, Object>) analyticsData.get("historical_overview");
            if (historicalOverview != null) {
                htmlContent.append("<div class='section'>\n");
                htmlContent.append("<h2>📚 Historical Overview</h2>\n");
                htmlContent.append("<p><strong>Analysis Type:</strong> ").append(historicalOverview.get("analysis_type")).append("</p>\n");
                htmlContent.append("<div style='display: flex; flex-wrap: wrap;'>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(historicalOverview.get("total_transactions_analyzed")).append("</div>\n");
                htmlContent.append("<div>Total Historical Transactions</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric currency'>").append(String.format("%.2f RSD", ((Number) historicalOverview.get("total_amount_analyzed_rsd")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Total Historical Amount</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(String.format("%.2f", ((Number) historicalOverview.get("average_transaction_value")).doubleValue())).append("</div>\n");
                htmlContent.append("<div>Historical Avg (RSD)</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("<div class='stat-box'>\n");
                htmlContent.append("<div class='metric'>").append(historicalOverview.get("unique_active_users")).append("</div>\n");
                htmlContent.append("<div>Unique Active Users</div>\n");
                htmlContent.append("</div>\n");
                htmlContent.append("</div>\n");
                
                // Historical top users table (limited to top 5)
                List<Map<String, Object>> historicalUsers = (List<Map<String, Object>>) historicalOverview.get("top_users_by_activity");
                if (historicalUsers != null && !historicalUsers.isEmpty()) {
                    htmlContent.append("<h3>🌟 Historical Top Users (Top 5)</h3>\n");
                    htmlContent.append("<table>\n");
                    htmlContent.append("<tr>\n");
                    htmlContent.append("<th>User ID</th>\n");
                    htmlContent.append("<th>Total Transactions</th>\n");
                    htmlContent.append("<th>Total Amount (RSD)</th>\n");
                    htmlContent.append("<th>Avg Transaction (RSD)</th>\n");
                    htmlContent.append("<th>Active Days</th>\n");
                    htmlContent.append("<th>Activity Intensity</th>\n");
                    htmlContent.append("</tr>\n");
                    
                    int historicalUserLimit = Math.min(5, historicalUsers.size());
                    for (int i = 0; i < historicalUserLimit; i++) {
                        Map<String, Object> user = historicalUsers.get(i);
                        htmlContent.append("<tr>\n");
                        htmlContent.append("<td>").append(user.get("user_id").toString().substring(0, 8)).append("...</td>\n");
                        htmlContent.append("<td>").append(user.get("total_transactions")).append("</td>\n");
                        htmlContent.append("<td class='currency'>").append(String.format("%.2f", ((Number) user.get("total_amount_rsd")).doubleValue())).append("</td>\n");
                        htmlContent.append("<td>").append(String.format("%.2f", ((Number) user.get("avg_transaction_value")).doubleValue())).append("</td>\n");
                        htmlContent.append("<td>").append(user.get("active_days")).append("</td>\n");
                        htmlContent.append("<td>").append(String.format("%.2f", ((Number) user.get("activity_intensity")).doubleValue())).append("</td>\n");
                        htmlContent.append("</tr>\n");
                    }
                    htmlContent.append("</table>\n");
                }
                htmlContent.append("</div>\n");
            }
            
            // Footer
            htmlContent.append("<div style='margin-top: 40px; padding: 20px; background: #ecf0f1; border-radius: 8px; text-align: center;'>\n");
            htmlContent.append("<p style='margin: 0; color: #7f8c8d;'>Report generated by Enhanced Transaction Analytics System</p>\n");
            htmlContent.append("<p style='margin: 5px 0 0 0; color: #7f8c8d; font-size: 12px;'>Cassandra Database • Spring Boot Microservice • Advanced Analytics</p>\n");
            htmlContent.append("</div>\n");
            
            htmlContent.append("</body>\n</html>");
            
            return htmlContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("=== PDF GENERATOR === Error generating enhanced analytics PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate enhanced analytics PDF", e);
        }
    }
}