package com.cosmetics.admin.search;

import com.cosmetics.admin.productVariant.ProductVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantReindexRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductVariantReindexRunner.class);

    private final ProductVariantService productVariantService;

    @Value("${app.elasticsearch.reindex-on-startup:true}")
    private boolean reindexOnStartup;

    public ProductVariantReindexRunner(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean requestedViaArg = args.containsOption("reindex-product-variants");
        if (!reindexOnStartup && !requestedViaArg) {
            return;
        }

        LOGGER.info("Starting bulk import of product variants into Elasticsearch index...");
        productVariantService.reindexAllVariantsToElastic();
        LOGGER.info("Finished bulk import of product variants.");
    }
}
