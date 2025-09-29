package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "transactions")
public class Transaction {
    
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    
    @Field(type = FieldType.Keyword)
    private String cardId;
    
    @Field(type = FieldType.Keyword)
    private String merchantId;
    
    @Field(type = FieldType.Double)
    private Double amount;
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||strict_date_optional_time||epoch_millis")
    private String date;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
}