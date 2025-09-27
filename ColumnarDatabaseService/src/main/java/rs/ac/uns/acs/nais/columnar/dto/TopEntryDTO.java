package rs.ac.uns.acs.nais.columnar.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEntryDTO {
    private UUID id;
    private String name;      // može ostati null ako nemaš naziv
    private long amountCents;
    private long txCount;
}
