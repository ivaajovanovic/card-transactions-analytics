package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public record TopMerchantView(
        String merchantId,
        String merchant,
        Long txCount,
        Double posAmt,
        Double onlineAmt,
        Double total
) {
}
