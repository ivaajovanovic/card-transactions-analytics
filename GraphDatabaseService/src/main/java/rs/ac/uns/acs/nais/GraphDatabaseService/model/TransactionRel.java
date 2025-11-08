package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.*;
import java.time.Instant;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRel {
    @Id
    @GeneratedValue
    private Long id;

    private Instant timestamp;
    private Double amount;
    private Currency currency;
    private Double originalAmount; 
    private Currency originalCurrency;
    private Double fxRate;

    private PaymentType paymentType;
    private PaymentPurpose purpose;
    private TransactionStatus status;
    private DeclineReason declineReason; 
    private String authCode;
    private Channel channel; // IN_STORE/WEB/APP/PHONE
    private Boolean contactless;

    @TargetNode
    private MerchantNode merchant;
    
}
