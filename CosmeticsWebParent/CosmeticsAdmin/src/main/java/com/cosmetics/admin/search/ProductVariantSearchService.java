package com.cosmetics.admin.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.cosmetics.common.entity.product.ProductVariantDocument;
import org.springframework.stereotype.Service;

@Service
public class ProductVariantSearchService {
    private final ElasticsearchClient client;
    private final String indexName = "product_variants";

    public ProductVariantSearchService(ElasticsearchClient client) {
        this.client = client;
    }

    public void indexVariant(ProductVariantDocument doc) throws Exception {
        client.index(i -> i
                .index(indexName)
                .id(doc.getId())
                .document(doc)
        );
    }
}
