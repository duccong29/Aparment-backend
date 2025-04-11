package apartments.entity;

import jakarta.persistence.Id;
import jdk.jshell.Snippet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Document(indexName = "apartments")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApartmentDocument {
    @Id
    String id;

    @Field(type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard")
    String title;

    @Field(type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard")
    String description;

    @Field(type = FieldType.Double)
    Double price;

    @Field(type = FieldType.Double)
    Double area;

    @Field(type = FieldType.Keyword)
    String status;

    @Field(type = FieldType.Keyword)
    String userName;

    @Field(type = FieldType.Object)
    LocationDocument location;

    @Field(type = FieldType.Keyword)
    String apartmentTypeName;

    @Field(type = FieldType.Nested)
    List<ImageDocument> images;

    @Field(type = FieldType.Date)
    Instant createdDate;

    @Field(type = FieldType.Date)
    Instant modifiedDate;

}
