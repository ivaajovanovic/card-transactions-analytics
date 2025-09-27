package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class TransactionDto {
  private String id;
  private Double amount;
  private OffsetDateTime date;
  private String description;
  private String merchantId; // za lakše setovanje PROCESSED_AT
}
