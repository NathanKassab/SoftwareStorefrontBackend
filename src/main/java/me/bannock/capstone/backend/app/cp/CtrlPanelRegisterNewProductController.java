package me.bannock.capstone.backend.app.cp;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/registerNewProduct/")
@Secured("PRIV_REGISTER_PRODUCT")
public class CtrlPanelRegisterNewProductController {

}
