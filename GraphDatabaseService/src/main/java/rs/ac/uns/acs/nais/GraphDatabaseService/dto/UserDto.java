package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserDto {
  private String id;
  private String name;
  private String email;
}
