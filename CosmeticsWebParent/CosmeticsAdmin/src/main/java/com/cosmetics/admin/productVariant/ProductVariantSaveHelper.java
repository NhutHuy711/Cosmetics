package com.cosmetics.admin.productVariant;

import com.cosmetics.admin.AmazonS3Util;
import com.cosmetics.common.entity.product.ProductImage;
import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductVariantSaveHelper {
    static void setMainImageName(MultipartFile mainImageMultipart, ProductVariant productVariant) {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
            productVariant.setMainImage(fileName);
        }
    }

    static void setNewExtraImageNames(MultipartFile[] extraImageMultiparts, ProductVariant productVariant) {
        if (extraImageMultiparts.length > 0) {
            for (MultipartFile multipartFile : extraImageMultiparts) {
                if (!multipartFile.isEmpty()) {
                    String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

                    if (!productVariant.containsImageName(fileName)) {
                        productVariant.addExtraImage(fileName);
                    }
                }
            }
        }
    }

    static void setExistingExtraImageNames(String[] imageIDs, String[] imageNames,
                                           ProductVariant productVariant) {
        if (imageIDs == null || imageIDs.length == 0 || imageNames == null) {
            productVariant.setImages(new HashSet<>());
            return;
        }

        Set<ProductImage> images = new HashSet<>();

        int count = Math.min(imageIDs.length, imageNames.length);

        for (int i = 0; i < count; i++) {
            String imageID = imageIDs[i].trim();
            String imageName = imageNames[i].trim();

            if (!imageID.isEmpty() && !imageName.isEmpty()) {
                try {
                    Integer id = Integer.parseInt(imageID);
                    ProductImage productImage = new ProductImage();
                    productImage.setId(id);
                    productImage.setName(imageName);
                    images.add(productImage);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid image ID: " + imageID);
                }
            }
        }

        productVariant.setImages(images);
    }

    static void saveUploadedImages(MultipartFile mainImageMultipart,
                                   MultipartFile[] extraImageMultiparts, ProductVariant savedProductVariant) throws IOException {
        if (!mainImageMultipart.isEmpty()) {
            String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
            String uploadDir = "product-images/" + savedProductVariant.getId();

            List<String> listObjectKeys = AmazonS3Util.listFolder(uploadDir + "/");
            for (String objectKey : listObjectKeys) {
                if (!objectKey.contains("/extras/")) {
                    AmazonS3Util.deleteFile(objectKey);
                }
            }

            AmazonS3Util.uploadFile(uploadDir, fileName, mainImageMultipart.getInputStream());
        }

        if (extraImageMultiparts.length > 0) {
            String uploadDir = "product-images/" + savedProductVariant.getId() + "/extras";

            for (MultipartFile multipartFile : extraImageMultiparts) {
                if (multipartFile.isEmpty()) continue;

                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
            }
        }

    }

    static void deleteExtraImagesWeredRemovedOnForm(ProductVariant productVariant) {
        String extraImageDir = "product-images/" + productVariant.getId() + "/extras";
        List<String> listObjectKeys = AmazonS3Util.listFolder(extraImageDir);

        for (String objectKey : listObjectKeys) {
            int lastIndexOfSlash = objectKey.lastIndexOf("/");
            String fileName = objectKey.substring(lastIndexOfSlash + 1, objectKey.length());

            if (!productVariant.containsImageName(fileName)) {
                AmazonS3Util.deleteFile(objectKey);
                System.out.println("Deleted extra image: " + objectKey);
            }
        }
    }
}
