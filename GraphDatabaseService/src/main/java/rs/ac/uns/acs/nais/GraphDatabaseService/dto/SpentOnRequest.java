package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Channel;

@Data @NoArgsConstructor @AllArgsConstructor
public class SpentOnRequest {
  private String txId;
  private Channel channel;     // POS/ECOM
  private Boolean cardPresent; // true/false
}
