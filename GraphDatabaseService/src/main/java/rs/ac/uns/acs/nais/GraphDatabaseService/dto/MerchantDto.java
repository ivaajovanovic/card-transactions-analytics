package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class MerchantDto {
  private String id;
  private String name;
  private String location;
  private String categoryId; // BELONGS_TO
}
