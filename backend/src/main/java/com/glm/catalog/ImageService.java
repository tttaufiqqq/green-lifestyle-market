package com.glm.catalog;

import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.ProductImage;
import com.glm.catalog.repository.ProductImageRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final int MAX_IMAGES = 5;

    private final ProductImageRepository imageRepo;
    private final Path uploadRoot;

    public ImageService(ProductImageRepository imageRepo,
                        @Value("${app.upload-dir:/var/glm/uploads}") String uploadDir) {
        this.imageRepo = imageRepo;
        this.uploadRoot = Path.of(uploadDir);
    }

    public ProductImage store(Product product, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType))
            throw new DomainException(ErrorCode.E_UPLOAD_TYPE, "Image must be JPEG/PNG/WebP", 400);
        if (file.getSize() > MAX_BYTES)
            throw new DomainException(ErrorCode.E_UPLOAD_SIZE, "Image must be \u2264 2 MB", 400);

        List<ProductImage> existing = imageRepo.findByProductIdOrderBySortOrderAsc(product.getId());
        if (existing.size() >= MAX_IMAGES)
            throw new DomainException(ErrorCode.E_UPLOAD_SIZE, "Maximum 5 images per listing", 400);

        Path dir = uploadRoot.resolve("products/" + product.getId());
        Files.createDirectories(dir);

        String filename;
        Path target;
        if ("image/webp".equals(contentType)) {
            filename = UUID.randomUUID() + ".webp";
            target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            filename = UUID.randomUUID() + ".jpg";
            target = dir.resolve(filename);
            Thumbnails.of(file.getInputStream())
                    .size(1200, 1200)
                    .outputFormat("jpg")
                    .toFile(target.toFile());
        }

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setPath("products/" + product.getId() + "/" + filename);
        img.setSortOrder(existing.size());
        img.setPrimary(existing.isEmpty());
        return imageRepo.save(img);
    }

    public void delete(ProductImage img) throws IOException {
        Files.deleteIfExists(uploadRoot.resolve(img.getPath()));
        imageRepo.delete(img);
    }
}
