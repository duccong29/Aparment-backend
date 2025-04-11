package apartments.dao;

import apartments.entity.ApartmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ApartmentElasticsearchRepository extends ElasticsearchRepository<ApartmentDocument, String> {

}
