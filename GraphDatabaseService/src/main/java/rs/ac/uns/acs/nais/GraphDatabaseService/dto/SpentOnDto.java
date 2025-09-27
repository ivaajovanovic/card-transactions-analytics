package rs.ac.uns.acs.nais.GraphDatabaseService.dto;
import lombok.Data;

@Data public class SpentOnDto {
  private String cardId;    // cid
  private String txId;      // tid
  private String channel;   // "POS"/"ECOM"
  private Boolean cardPresent;
}
