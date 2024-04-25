package me.bannock.capstone.backend.products.service.db;

import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DaoProductServiceImpl implements ProductService {

    @Autowired
    public DaoProductServiceImpl(ProductRepo productRepo){
        this.productRepo = productRepo;
    }

    private final Logger logger = LogManager.getLogger();

    private final ProductRepo productRepo;

    @Value("${backend.productService.defaultProductName}")
    private String defaultProductName;

    @Value("${backend.productService.defaultProductdescription}")
    private String defaultProductDescription;

    @Value("${backend.productService.defaultProductIconUrl}")
    private String defaultProductIconUrl;

    @Value("${backend.productService.defaultProductPurchaseUrl}")
    private String defaultProductPurchaseUrl;

    @Value("${backend.productService.defaultProductPrice}")
    private double defaultProductPrice;

    @Value("${backend.productService.maxProductNameLength}")
    private int maxProductNameLength;

    @Value("${backend.productService.maxProductDescriptionLength}")
    private int maxProductDescriptionLength;

    @Value("${backend.productService.maxProductIconUrlLength}")
    private int maxProductIconUrlLength;

    @Value("${backend.productService.maxProductPurchaseUrlLength}")
    private int maxProductPurchaseUrlLength;

    @Override
    public Optional<ProductDTO> getProductDetails(long productId) throws ProductServiceException {
        Optional<ProductModel> product = productRepo.findById(productId);
        return product.map(this::mapToDto);
    }

    @Override
    public void setProductDetails(ProductDTO productDetails) throws ProductServiceException {
        Objects.requireNonNull(productDetails);

        Optional<ProductModel> product = productRepo.findById(productDetails.getId());
        if (product.isEmpty())
            throw new ProductServiceException("Could not find product", null, productDetails.getId());

        // We validate fields before actually making changes
        if (productDetails.getName().length() > maxProductNameLength)
            throw new ProductServiceException(
                    "Name cannot exceed %s characters".formatted(maxProductNameLength), productDetails);

        if (productDetails.getDescription().length() > maxProductDescriptionLength)
            throw new ProductServiceException(
                    "Description cannot exceed %s characters".formatted(maxProductDescriptionLength), productDetails);

        if (productDetails.getIconUrl().length() > maxProductIconUrlLength)
            throw new ProductServiceException(
                    "Icon URL cannot exceed %s characters".formatted(maxProductIconUrlLength), productDetails);

        if (productDetails.getPurchaseUrl().length() > maxProductPurchaseUrlLength)
            throw new ProductServiceException(
                    "Purchase URL cannot exceed %s characters".formatted(maxProductPurchaseUrlLength), productDetails);

        mergeIntoModel(product.get(), productDetails);
        productRepo.saveAndFlush(product.get());
    }

    @Override
    public long registerProduct(long ownerId) throws ProductServiceException {
        ProductModel product = new ProductModel(
                ownerId,
                defaultProductName,
                defaultProductDescription,
                defaultProductIconUrl,
                defaultProductPurchaseUrl,
                defaultProductPrice
        );
        productRepo.saveAndFlush(product);
        logger.info("User has registered a new product, uid={}, product={}", ownerId, product);
        return product.getId();
    }

    @Override
    public List<ProductDTO> getUsersProducts(long ownerId) throws ProductServiceException {
        return productRepo.findProductModelsByOwnerUid(ownerId).stream().map(this::mapToDto).toList();
    }

    /**
     * Maps a product model to a DTO object
     * @param model The model to map
     * @return The DTO equivalent
     */
    private ProductDTO mapToDto(ProductModel model){
        return new ProductDTO(
                model.getId(),
                model.getName(),
                model.getIconUrl(),
                model.getPurchaseUrl(),
                model.getDescription(),
                model.getOwnerUid(),
                model.isDisabled(),
                model.isHidden(),
                model.isApproved(),
                model.getPrice()
        );
    }

    /**
     * Merges data from the dto into the model
     * @param model The model to merge into
     * @param dto The dto to get the data from
     */
    private void mergeIntoModel(ProductModel model, ProductDTO dto){
        model.setName(dto.getName());
        model.setIconUrl(dto.getIconUrl());
        model.setPurchaseUrl(dto.getPurchaseUrl());
        model.setDescription(dto.getDescription());
        model.setOwnerUid(dto.getOwnerUid());
        model.setDisabled(dto.isDisabled());
        model.setHidden(dto.isHidden());
        model.setApproved(dto.isApproved());
        model.setPrice(dto.getPrice());
    }

}
