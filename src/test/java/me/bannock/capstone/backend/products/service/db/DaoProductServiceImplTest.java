package me.bannock.capstone.backend.products.service.db;

import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DaoProductServiceImplTest {

    @Autowired
    private DaoProductServiceImpl productService;

    private final long TEST_OWNER_ID = 0;

    @Value("${backend.productService.maxProductNameLength}")
    private int maxProductNameLength;

    @Value("${backend.productService.maxProductDescriptionLength}")
    private int maxProductDescriptionLength;

    @Value("${backend.productService.maxProductIconUrlLength}")
    private int maxProductIconUrlLength;

    @Value("${backend.productService.maxProductPurchaseUrlLength}")
    private int maxProductPurchaseUrlLength;

    @Value("${backend.productService.maxKeygenIdLength}")
    private int maxKeygenIdLength;

    @Test
    @WithMockUser(authorities = {"PRIV_REGISTER_PRODUCT"})
    void getUserProducts() throws ProductServiceException {
        int initialProductCount = productService.getUsersProducts(TEST_OWNER_ID).size();
        registerProduct();
        registerProduct();
        registerProduct();
        assertEquals(initialProductCount + 3, productService.getUsersProducts(TEST_OWNER_ID).size());
    }

    @Test
    @WithMockUser(authorities = {"PRIV_REGISTER_PRODUCT", "PRIV_MODIFY_PRODUCT_DETAILS"})
    void registerGetAndUpdateProduct() throws ProductServiceException {
        // Register
        long productId = registerProduct();

        // Get
        Optional<ProductDTO> product = productService.getProductDetails(productId);
        assertTrue(product.isPresent());

        // Update
        String newName = product.get().getName() + "_";
        String newDesc = product.get().getDescription() + "_";
        String newUrls = "test.test.test.test.example.com";
        String newKeygen = product.get().getKeygenId() + "_";
        boolean disabled = !product.get().isDisabled();
        boolean hidden = !product.get().isHidden();
        boolean approved = !product.get().isApproved();
        double price = product.get().getPrice();
        product.get().setName(newName);
        product.get().setDescription(newDesc);
        product.get().setIconUrl(newUrls);
        product.get().setPurchaseUrl(newUrls);
        product.get().setKeygenId(newKeygen);
        product.get().setDisabled(disabled);
        product.get().setHidden(hidden);
        product.get().setApproved(approved);
        product.get().setPrice(price);
        productService.setProductDetails(product.get());
        // Overwrite last product object with the one fetched from the database
        product = productService.getProductDetails(productId);

        assertTrue(product.isPresent());
        assertEquals(product.get().getName(), newName);
        assertEquals(product.get().getDescription(), newDesc);
        assertEquals(product.get().getIconUrl(), newUrls);
        assertEquals(product.get().getPurchaseUrl(), newUrls);
        assertEquals(product.get().getKeygenId(), newKeygen);
        assertEquals(product.get().isDisabled(), disabled);
        assertEquals(product.get().isHidden(), hidden);
        assertEquals(product.get().isApproved(), approved);
        assertEquals(product.get().getPrice(), price);

        // Now we test by sending bad data
        String badName = new String(new char[maxProductNameLength + 1]);
        String badDesc = new String(new char[maxProductDescriptionLength + 1]);
        String badIconUrl = new String(new char[maxProductIconUrlLength + 1]);
        String badPurchaseUrl = new String(new char[maxProductPurchaseUrlLength + 1]);
        String badKeygenId = new String(new char[maxKeygenIdLength + 1]);

        product.get().setName(badName);
        final ProductDTO finalProduct1 = product.get();
        assertThrows(ProductServiceException.class, () -> productService.setProductDetails(finalProduct1));
        product = productService.getProductDetails(productId);
        assertTrue(product.isPresent());

        product.get().setDescription(badDesc);
        final ProductDTO finalProduct2 = product.get();
        assertThrows(ProductServiceException.class, () -> productService.setProductDetails(finalProduct2));
        product = productService.getProductDetails(productId);
        assertTrue(product.isPresent());

        product.get().setIconUrl(badIconUrl);
        final ProductDTO finalProduct3 = product.get();
        assertThrows(ProductServiceException.class, () -> productService.setProductDetails(finalProduct3));
        product = productService.getProductDetails(productId);
        assertTrue(product.isPresent());

        product.get().setPurchaseUrl(badPurchaseUrl);
        final ProductDTO finalProduct4 = product.get();
        assertThrows(ProductServiceException.class, () -> productService.setProductDetails(finalProduct4));

        product.get().setKeygenId(badKeygenId);
        final ProductDTO finalProduct5 = product.get();
        assertThrows(ProductServiceException.class, () -> productService.setProductDetails(finalProduct5));

        // Double check to ensure the changes didn't go through
        product = productService.getProductDetails(productId);
        assertTrue(product.isPresent());
        assertNotEquals(product.get().getName(), badName);
        assertNotEquals(product.get().getDescription(), badIconUrl);
        assertNotEquals(product.get().getIconUrl(), badIconUrl);
        assertNotEquals(product.get().getPurchaseUrl(), badPurchaseUrl);

    }

    @Test
    @WithMockUser(authorities = {"PRIV_REGISTER_PRODUCT"})
    public void registerProductTest(){
        assertDoesNotThrow(this::registerProduct);
    }

    /**
     * Registers a new product
     * @return The product's id
     */
    private long registerProduct() throws ProductServiceException {
        return productService.registerProduct(TEST_OWNER_ID);
    }

}