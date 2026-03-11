package com.cosmetics.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import javax.annotation.PostConstruct;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductVariantIndexInitializer {

    private final ElasticsearchClient client;

    public ProductVariantIndexInitializer(@Autowired ElasticsearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws Exception {
        final String index = "product_variants";

        boolean exists = client.indices().exists(i -> i.index(index)).value();

        if (exists) {
            var mappingResp = client.indices().getMapping(g -> g.index(index));
            var props = mappingResp.result().get(index).mappings().properties();
            var optionsProp = props != null ? props.get("options") : null;

            // Nếu options đã tồn tại nhưng KHÔNG phải nested -> xóa index để tạo lại đúng
            if (optionsProp != null && !optionsProp.isNested()) {
                client.indices().delete(d -> d.index(index));
                exists = false;
            }
        }

        if (!exists) {
            client.indices().create(c -> c
                    .index(index)
                    .mappings(m -> m
                            .properties("name", p -> p.text(t -> t))
                            .properties("productName", p -> p.text(t -> t))
                            .properties("brand", p -> p.text(t -> t))
                            .properties("category", p -> p.text(t -> t))
                            .properties("description", p -> p.text(t -> t))
                            .properties("price", p -> p.float_(t -> t))
                            .properties("finalPrice", p -> p.float_(t -> t))
                            .properties("discountPercent", p -> p.float_(t -> t))
                            .properties("averageRating", p -> p.float_(t -> t))   // khớp field bạn đang set
                            .properties("reviewCount", p -> p.long_(t -> t))
                            .properties("mainImage", p -> p.keyword(t -> t))
                            // options nested
                            .properties("options", p -> p.nested(n -> n
                                    .properties("type", pp -> pp.text(tt -> tt))
                                    .properties("value", pp -> pp.text(tt -> tt))
                                    .properties("typeId", pp -> pp.long_(tt -> tt))
                                    .properties("valueId", pp -> pp.long_(tt -> tt))
                            ))
                    )
            );
        }
    }

}

