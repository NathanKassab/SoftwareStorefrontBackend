package me.bannock.capstone.backend.app.cp;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/downloadLoader/")
@Secured("PRIV_LAUNCH_LOADER")
public class CtrlDownloadLoader {
}
