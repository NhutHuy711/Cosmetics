package com.cosmetics.admin.storage;

import com.cosmetics.admin.MessageServiceAdmin;
import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.admin.product.ProductService;
import com.cosmetics.admin.productVariant.ProductVariantService;
import com.cosmetics.admin.user.UserService;
import com.cosmetics.common.entity.User;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.entity.storage.Import;
import com.cosmetics.common.exception.ImportNotFoundException;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ImportService {
    public static final int IMPORT_PER_PAGE = 5;

    @Autowired
    private ImportRepository importRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MessageServiceAdmin messageService;

    @Autowired
    private ProductVariantService productVariantService;

    public List<Import> listAll() {return (List<Import>) importRepo.findAll();}

    public void listByPage(int pageNum, PagingAndSortingHelper helper){
        helper.listEntities(pageNum, IMPORT_PER_PAGE, importRepo);
    }

    public Import get(Integer id) throws ImportNotFoundException{
        try {
            return importRepo.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new ImportNotFoundException(messageService.getMessage("IMPORT_NOT_FOUND") + " " + id);
        }
    }

    public void setImportDetails(String[] detailProductVariantIds, String[] detailAmounts,
         String[] detailCosts, Import ip) throws ProductVariantNotFoundException {
        if (detailProductVariantIds == null || detailProductVariantIds.length == 0) return;

        int detailCount = Math.min(detailProductVariantIds.length,
                Math.min(lengthOf(detailAmounts), lengthOf(detailCosts)));

        if (detailCount == 0) return;


        ip.getDetails().clear();

        for (int count = 0; count < detailCount; count++) {
            String productVariantIdValue = trimOrNull(detailProductVariantIds[count]);
            String quantityValue = trimOrNull(detailAmounts[count]);
            String costValue = trimOrNull(detailCosts[count]);

            if (productVariantIdValue == null || quantityValue == null || costValue == null) {
                continue;
            }

            int productVariantId = Integer.parseInt(productVariantIdValue);
            ProductVariant variant = productVariantService.get(productVariantId);
            Integer quantity = Integer.parseInt(quantityValue);
            Float unitCost = Float.parseFloat(costValue);
            Float totalCost = quantity * unitCost;

            ip.addDetail(variant, quantity, unitCost);
        }
    }

    public Import save(Import ip, String emailUser){
        User user = userService.getByEmail(emailUser);
        if(ip.getId() == null){
            ip.setTransactionTime(new Date());
            ip.setUser(user);
        }
        return importRepo.save(ip);
    }

    private static int lengthOf(String[] array) {
        return array == null ? 0 : array.length;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
