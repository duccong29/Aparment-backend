package apartments.service;


import apartments.dto.response.PageResponse;
import apartments.entity.ApartmentDocument;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import co.elastic.clients.json.JsonData;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchService {
    ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "apartments";

    public PageResponse<ApartmentDocument> searchApartments(
            String title,
            Double minPrice,
            Double maxPrice,
            String apartmentTypeName,
            int page,
            int size) {

        try {
            int from = (page - 1) * size;

            SearchResponse<ApartmentDocument> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .bool(b -> {
                                        if (title != null && !title.isEmpty()) {
                                            b.must(m -> m
                                                    .match(t -> t
                                                            .field("title")
                                                            .query(title)
                                                            .fuzziness("AUTO")
                                                    ));
                                        }
                                        if (minPrice != null) {
                                            b.filter(f -> f.range(r -> r.field("price").gte(JsonData.of(minPrice))));
                                        }
                                        if (maxPrice != null) {
                                            b.filter(f -> f.range(r -> r.field("price").lte(JsonData.of(maxPrice))));
                                        }
                                        if (apartmentTypeName != null && !apartmentTypeName.isEmpty()) {
                                            b.filter(f -> f.term(t -> t.field("apartmentTypeName").value(apartmentTypeName)));
                                        }
                                        return b;
                                    })
                            )
                            .from(from)
                            .size(size),
                    ApartmentDocument.class);

            long totalElements = response.hits().total().value();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            return PageResponse.<ApartmentDocument>builder()
                    .currentPage(page)
                    .totalPages(totalPages)
                    .pageSize(size)
                    .totalElements(totalElements)
                    .data(response.hits().hits().stream()
                            .map(Hit::source)
                            .collect(Collectors.toList()))
                    .build();
        } catch (IOException e) {
            log.error("Error occurred while searching apartments", e);
            throw new AppException(ErrorCodes.IO_EXCEPTION);
        }
    }

    public List<String> suggestTitles(String prefix) {
        try {
            SearchResponse<ApartmentDocument> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .matchPhrasePrefix(m -> m  // Sử dụng match_phrase_prefix để gợi ý
                                            .field("title")
                                            .query(prefix)
                                            .maxExpansions(10)  // Giới hạn số lượng gợi ý
                                    )
                            )
                            .size(5),  // Lấy 5 kết quả đầu tiên
                    ApartmentDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source().getTitle())
                    .distinct()  // Loại bỏ trùng lặp
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AppException(ErrorCodes.IO_EXCEPTION);
        }
    }
}
