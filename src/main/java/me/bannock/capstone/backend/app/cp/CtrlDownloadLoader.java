package me.bannock.capstone.backend.app.cp;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtJobDto;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtService;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.util.Optional;

@Controller
@RequestMapping("/app/downloadLoader/")
@Secured("PRIV_LAUNCH_LOADER")
public class CtrlDownloadLoader {

    @Autowired
    public CtrlDownloadLoader(LoaderProtService loaderProtService, UserService userService){
        this.loaderProtService = loaderProtService;
        this.userService = userService;
    }

    private final Logger logger = LogManager.getLogger();
    private final String PAGE_PATH = "/app/downloadLoader";
    private final LoaderProtService loaderProtService;
    private final UserService userService;

    @PostMapping("build")
    @Secured("PRIV_LAUNCH_LOADER")
    public ResponseEntity<String> build(){
        // TODO: Implement and add javascript on download page to download
        return null;
    }

    @GetMapping("status/{jobId}")
    public ResponseEntity<String> status(HttpServletRequest request, @PathVariable(name = "jobId") String jobId){
        Optional<LoaderProtJobDto> dto = loaderProtService.getJobDto(jobId);
        Optional<File> outputFile = loaderProtService.getFinishedJobOutput(jobId);
        if (dto.isEmpty())
            return ResponseEntity.badRequest().body("Job id does not exist in our systems");

        // We must check that the user is allowed to access the requested job instance
        Optional<AccountDTO> userDto = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (userDto.isEmpty()){
            logger.warn("Could not find user, sessionId={}", request.getSession().getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user. Please try logging out and back in.");
        }
        else if (!loaderProtService.canUserAccessJob(userDto.get().getUid(), dto.get().getId())){
            logger.warn("User attempted to access someone else's loader job status, sessionId={}, user={}",
                    request.getSession().getId(), userDto.get());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user. Please try logging out and back in.");
        }

        if (outputFile.isPresent())
            return ResponseEntity.ok(dto.get().getState());
        return ResponseEntity.accepted().body(dto.get().getState());
    }

    // TODO: Add download endpoint

}
