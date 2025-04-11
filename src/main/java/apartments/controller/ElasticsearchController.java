package apartments.controller;

import apartments.dao.ApartmentElasticsearchRepository;
import apartments.entity.ApartmentDocument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticsearchController {
    ApartmentElasticsearchRepository elasticsearchRepository;


    @KafkaListener(topics = "apartments-topic")
    public void listenApartmentsDocument(ApartmentDocument document) {
        elasticsearchRepository.save(document);
        log.info("Apartment data saved to Elasticsearch: {}", document);
    }

    @KafkaListener(topics = "apartments-delete-topic")
    public void listenForDelete(String apartmentId) {
        log.info("Received delete request for apartment with ID: {}", apartmentId);

        try {
            elasticsearchRepository.deleteById(apartmentId);
            log.info("Successfully deleted apartment with ID: {} from Elasticsearch", apartmentId);
        } catch (Exception e) {
            log.error("Failed to delete apartment with ID: {} from Elasticsearch", apartmentId, e);
        }
    }



}
