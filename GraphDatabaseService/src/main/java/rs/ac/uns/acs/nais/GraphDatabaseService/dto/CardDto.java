package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.CardType;

@Data @NoArgsConstructor @AllArgsConstructor
public class CardDto {
  private String id;
  private CardType type;   // CREDIT/DEBIT
  private Double limit;
}
