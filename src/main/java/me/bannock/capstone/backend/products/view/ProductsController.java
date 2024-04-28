package me.bannock.capstone.backend.products.view;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/products/")
public class ProductsController {

    @Autowired
    public ProductsController(ProductService productService){
        this.productService = productService;
    }

    private final Logger logger = LogManager.getLogger();
    private final ProductService productService;

    @GetMapping("view/{productId}")
    public String productLander(HttpServletRequest request,
                                @PathVariable(name = "productId") long productId,
                                Model model){

        try {
            Optional<ProductDTO> product = productService.getProductDetails(productId);

            // Validate the product and ensure it should be publicly shown
            if (product.isEmpty()){
                logger.info("User attempted to access nonexistent product lander, productId={}, sessionId={}",
                        product, request.getSession().getId());
                return "products/notFound";
            }
            if (product.get().isDisabled() || product.get().isHidden()){
                logger.info("User attempted to access product that is disabled or hidden, productId={}, sessionId={}",
                        productId, request.getSession().getId());
                return "products/unavailable";
            }

            model.addAttribute("product", product.get());
        } catch (ProductServiceException e) {
            logger.error(
                    "Something went wrong while displaying a product lander page, e={}, productId={}, sessionId={}",
                    e, productId, request.getSession().getId());
            throw new RuntimeException(e);
        }

        return "products/lander";
    }

}
