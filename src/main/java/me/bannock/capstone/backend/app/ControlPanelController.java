package me.bannock.capstone.backend.app;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import me.bannock.capstone.backend.security.Privilege;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/app/")
@Secured("PRIV_VIEW_MAIN_APP_PANEL")
public class ControlPanelController {

    @Autowired
    public ControlPanelController(UserService userService, ProductService productService, LicenseService licenseService){
        this.userService = userService;
        this.productService = productService;
        this.licenseService = licenseService;

        // This is a simple way of limiting some user to specific pages
        Map<String, Privilege[]> pages = new LinkedHashMap<>();
        pages.put("myAccount", new Privilege[]{Privilege.PRIV_VIEW_OWN_ACCOUNT_INFORMATION});
        pages.put("downloadLoader", new Privilege[]{Privilege.PRIV_LAUNCH_LOADER});
        pages.put("manageMyProducts", new Privilege[]{Privilege.PRIV_VIEW_OWN_PRODUCTS});
        pages.put("registerNewProduct", new Privilege[]{Privilege.PRIV_REGISTER_PRODUCT});
        this.pages = pages;

        // These pages are the same as the above ones, except they're not shown on the side nav
        Map<String, Privilege[]> unlistedPages = new LinkedHashMap<>();
        unlistedPages.put(ERROR_PAGE, new Privilege[0]);
        unlistedPages.put("product", new Privilege[0]);
        this.unlistedPages = unlistedPages;

        Map<String, String[]> stylesheets = new LinkedHashMap<>();
        stylesheets.put("products", new String[]{"/resources/css/panel/products.css"});
        stylesheets.put("product", new String[]{"/resources/css/productCard.css"});
        this.stylesheets = stylesheets;
    }

    private final Logger logger = LogManager.getLogger();
    public static final String ERROR_PAGE = "error";
    private final UserService userService;
    private final ProductService productService;
    private final LicenseService licenseService;
    private final Map<String, Privilege[]> pages, unlistedPages;
    private final Map<String, String[]> stylesheets;

    @GetMapping({"", "main", "main/", "main/{page}", "{page}"})
    public String main(
            HttpServletRequest request,
            @PathVariable(name = "page", required = false) String page,
            @RequestParam(name = "loggedIn", required = false, defaultValue = "false") boolean justLoggedIn,
            @RequestParam(name = "errorMessage", required = false) String errorMessage,
            @RequestParam(name = "productId", required = false) Long productId,
            Model model
    ) throws IOException {

        // If the user requests a nonexistent page, or if they request a page where they lack
        // needed privileges, we want to default to whatever placeholder page the template uses by
        // setting the page to null. However, if it is already null, we want to ignore that logic
        Map<String, Privilege[]> pages = new HashMap<>();
        pages.putAll(this.pages);
        pages.putAll(this.unlistedPages);
        if (page == null);
        else if (!pages.containsKey(page)){
            logger.info("User attempted to open app " +
                            "page that does not exist, user={}, sessionId={}, page={}",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    request.getSession().getId(), page);
            page = ERROR_PAGE;
            errorMessage = "Page does not exist";
        }else{
            Privilege[] neededPrivs = pages.get(page);
            if (!ControllerUtils.hasPrivs(neededPrivs)){
                logger.info("User attempted to open app " +
                        "page but lacked the needed privileges, user={}, sessionId={}, neededPrivs={}, page={}",
                        SecurityContextHolder.getContext().getAuthentication().getName(),
                        request.getSession().getId(), neededPrivs, page);
                page = ERROR_PAGE;
                errorMessage = "You do not have the needed permissions to view this page";
            }
        }

        // The sidebar is built using the mapping of pages we have in this class as well, so we
        // need to create a list of pages that the user is able to access
        model.addAttribute("sideNavPages", getPagesUserCanAccess());
        model.addAttribute("sideNavProducts", getProducts());

        Optional<AccountDTO> userDto = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (userDto.isEmpty()){
            logger.error("Could not find user account, sessionId={}, username={}",
                    request.getSession().getId(), SecurityContextHolder.getContext().getAuthentication().getName());
            throw new RuntimeException("Could not find user account");
        }
        model.addAttribute("userDto", userDto.get());

        if (productId != null){
            try{
                populateProductInformation(request, model, productId, userDto.get());
            }catch (ProductServiceException e){
                page = ERROR_PAGE;
                errorMessage = e.getMessage();
            }
        }

        // If the page is still not null by this point, it means
        // the user is opening up a specific page. We need to inject the link
        // to any stylesheets that may be linked with this page
        if (page != null && stylesheets.containsKey(page)){
            model.addAttribute("extraStylesheets", stylesheets.get(page));
        }else{
            model.addAttribute("extraStylesheets", null);
        }

        model.addAttribute("request", request);
        model.addAttribute("page", page);
        model.addAttribute("justLoggedIn", justLoggedIn);
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication());
        model.addAttribute("errorMessage", errorMessage == null ? "An unknown error has occurred. Please logout and try again." : errorMessage);
        return "app/controlPanel";
    }

    /**
     * Populates the model with product information as needed
     * @param request The request
     * @param model The model to populate
     * @param productId The product id
     * @param user The user requesting it
     * @throws ProductServiceException If something goes wrong while grabbing the product info
     */
    private void populateProductInformation(HttpServletRequest request, Model model, long productId, AccountDTO user) throws ProductServiceException{
        Optional<ProductDTO> productDto = productService.getProductDetails(productId);

        if (productDto.isEmpty() || productDto.get().isHidden() || productDto.get().isDisabled()){
            model.addAttribute("product", null);
        }
        else{
            Optional<AccountDTO> owner = userService.getAccountWithUid(productDto.get().getOwnerUid());
            if (owner.isEmpty()){
                logger.error("User attempted to access product where owner does not exist; " +
                                "this should never happen, productId={}, ownerUid={}, sessionId={}",
                        productId, productDto.get().getOwnerUid(), request.getSession().getId());
            }
            else{
                model.addAttribute("product", productDto.get());
                model.addAttribute("owner", owner.get());
                model.addAttribute("ownsProduct", licenseService.ownsProduct(user.getUid(), productId));
            }
        }
    }

    /**
     * Gets a list of products that should be shown to the user on the sidebar
     * @return The list of products
     */
    public List<ProductDTO> getProducts(){
        return productService.getDisplayProducts();
    }

    /**
     * @return The list of pages that the currently logged-in user can access
     */
    public List<String> getPagesUserCanAccess(){
        List<String> accessiblePages = new ArrayList<>();
        for (String page : this.pages.keySet()){
            if (ControllerUtils.hasPrivs(this.pages.get(page))){
                accessiblePages.add(page);
            }
        }
        return accessiblePages;
    }

}
