// src/main/java/.../dto/TransactionUpdateDTO.java
package rs.ac.uns.acs.nais.columnar.dto;

import java.util.UUID;

public record TransactionUpdateDTO(
    Long amountCents,
    String status,
    UUID merchantId,
    UUID categoryId
) {}
