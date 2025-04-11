package apartments.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageDocument {
    @Field(type = FieldType.Keyword)
    Long id;

    @Field(type = FieldType.Keyword)
    String url;
}
