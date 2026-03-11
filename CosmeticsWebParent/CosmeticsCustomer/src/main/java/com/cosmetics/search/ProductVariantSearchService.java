package com.cosmetics.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cosmetics.common.entity.product.ProductVariantDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductVariantSearchService {

    @Autowired
    private ElasticsearchClient client;

    private final String indexName = "product_variants";

    public void indexVariant(ProductVariantDocument doc) throws Exception {
        client.index(i -> i
                .index(indexName)
                .id(doc.getId())
                .document(doc)
        );
    }

    public List<ProductVariantDocument> search(String keyword) throws Exception {

        SearchResponse<ProductVariantDocument> response = client.search(s -> s
                        .index(indexName)
                        .query(q -> q.bool(b -> b
                                // 1) Search trên các field chính (root)
                                .should(sh -> sh.multiMatch(mm -> mm
                                        .query(keyword)
                                        .fields("name^3", "productName^2", "brand", "category", "description")
                                        .fuzziness("AUTO")
                                ))

                                // 2) Search trong options (nested)
                                .should(sh -> sh.nested(n -> n
                                        .path("options")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(keyword)
                                                .fields("options.type^2", "options.value^2")
                                                .fuzziness("AUTO")
                                        ))
                                ))

                                // Ít nhất 1 trong 2 should phải match
                                .minimumShouldMatch("1")
                        )),
                ProductVariantDocument.class
        );

        List<ProductVariantDocument> results = new ArrayList<>();
        for (Hit<ProductVariantDocument> hit : response.hits().hits()) {
            results.add(hit.source());
        }
        return results;
    }


}

