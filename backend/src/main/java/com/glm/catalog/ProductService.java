package com.glm.catalog;

import com.glm.catalog.dto.ListingRequest;
import com.glm.catalog.dto.ListingResponse;
import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.Product.Status;
import com.glm.catalog.entity.ProductImage;
import com.glm.catalog.repository.CategoryRepository;
import com.glm.catalog.repository.ProductImageRepository;
import com.glm.catalog.repository.ProductRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.common.security.GlmUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;
    private final CategoryRepository categoryRepo;
    private final ListingValidator validator;
    private final ImageService imageService;
    private final SlugGenerator slugGen;

    public ProductService(ProductRepository productRepo, ProductImageRepository imageRepo,
                          CategoryRepository categoryRepo, ListingValidator validator,
                          ImageService imageService, SlugGenerator slugGen) {
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.categoryRepo = categoryRepo;
        this.validator = validator;
        this.imageService = imageService;
        this.slugGen = slugGen;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(Long sellerId, String statusFilter) {
        List<Product> products = statusFilter != null
                ? productRepo.findBySellerIdAndStatus(sellerId, Status.valueOf(statusFilter))
                : productRepo.findBySellerId(sellerId);
        return products.stream()
                .map(p -> ListingResponse.from(p, imageRepo.findByProductIdOrderBySortOrderAsc(p.getId())))
                .toList();
    }

    @Transactional
    public ListingResponse create(ListingRequest req, GlmUserDetails principal) {
        validator.requireEmailVerified(principal.getUser());
        validator.validateFulfilment(req);
        Product p = new Product();
        p.setSeller(principal.getUser());
        applyRequest(p, req);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p = productRepo.save(p);
        p.setSlug(slugGen.generate(p.getTitle(), p.getId()));
        return ListingResponse.from(productRepo.save(p), List.of());
    }

    @Transactional
    public ListingResponse update(Long id, ListingRequest req, Long sellerId) {
        Product p = requireOwner(id, sellerId);
        validator.validateFulfilment(req);
        if (req.quantity() != p.getQuantity())
            validator.validateQtyNotBelowHeld(p, req.quantity());
        applyRequest(p, req);
        p.setUpdatedAt(Instant.now());
        applyAutoStatus(p);
        return ListingResponse.from(productRepo.save(p),
                imageRepo.findByProductIdOrderBySortOrderAsc(p.getId()));
    }

    @Transactional
    public ListingResponse patchStatus(Long id, String newStatus, Long sellerId) {
        Product p = requireOwner(id, sellerId);
        if ("DELETED".equals(newStatus)) {
            validator.requireNoOpenOrders(p);
            p.setStatus(Status.DELETED);
        } else {
            p.setStatus(Status.valueOf(newStatus));
        }
        p.setUpdatedAt(Instant.now());
        return ListingResponse.from(productRepo.save(p),
                imageRepo.findByProductIdOrderBySortOrderAsc(p.getId()));
    }

    @Transactional
    public ListingResponse addImage(Long id, MultipartFile file, Long sellerId) throws IOException {
        Product p = requireOwner(id, sellerId);
        imageService.store(p, file);
        return ListingResponse.from(p, imageRepo.findByProductIdOrderBySortOrderAsc(p.getId()));
    }

    @Transactional
    public void deleteImage(Long id, Long imgId, Long sellerId) throws IOException {
        requireOwner(id, sellerId);
        ProductImage img = imageRepo.findById(imgId)
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Image not found", 404));
        imageService.delete(img);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getAdminProducts(int page, int size) {
        return productRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(p -> ListingResponse.from(p,
                        imageRepo.findByProductIdOrderBySortOrderAsc(p.getId())));
    }

    @Transactional
    public ListingResponse adminPatchStatus(Long id, String newStatus) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Product not found", 404));
        p.setStatus(Status.valueOf(newStatus));
        p.setUpdatedAt(Instant.now());
        return ListingResponse.from(productRepo.save(p),
                imageRepo.findByProductIdOrderBySortOrderAsc(p.getId()));
    }

    void applyAutoStatus(Product p) {
        if (p.getQuantity() == 0 && p.getStatus() == Status.ACTIVE)
            p.setStatus(Status.SOLD_OUT);
        else if (p.getQuantity() > 0 && p.getStatus() == Status.SOLD_OUT)
            p.setStatus(Status.ACTIVE);
    }

    private void applyRequest(Product p, ListingRequest req) {
        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setItemCondition(req.itemCondition());
        p.setPrice(req.price());
        p.setQuantity(req.quantity());
        p.setAllowMeetup(req.allowMeetup());
        p.setAllowShipping(req.allowShipping());
        p.setShippingFee(req.shippingFee());
        p.setMeetupLocation(req.meetupLocation());
        p.setSustainabilityNote(req.sustainabilityNote());
        p.setCategory(categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Category not found", 404)));
        p.setStatus(req.status() != null ? Status.valueOf(req.status()) : Status.DRAFT);
    }

    private Product requireOwner(Long id, Long sellerId) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Listing not found", 404));
        if (!p.getSeller().getId().equals(sellerId))
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your resource", 403);
        return p;
    }
}
