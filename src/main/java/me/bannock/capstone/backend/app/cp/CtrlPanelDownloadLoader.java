package me.bannock.capstone.backend.app.cp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtJobDto;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtService;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping("/app/downloadLoader/")
@Secured("PRIV_LAUNCH_LOADER")
public class CtrlPanelDownloadLoader {

    @Autowired
    public CtrlPanelDownloadLoader(LoaderProtService loaderProtService, UserService userService){
        this.loaderProtService = loaderProtService;
        this.userService = userService;
    }

    private final Logger logger = LogManager.getLogger();
    private final String PAGE_PATH = "/app/downloadLoader";
    private final LoaderProtService loaderProtService;
    private final UserService userService;

    @Value("${backend.loader.downloadedFileName}")
    private String downloadedLoaderFileName;

    @PostMapping("build")
    @Secured("PRIV_LAUNCH_LOADER")
    public ResponseEntity<String> build(HttpServletRequest request){
        Optional<AccountDTO> user = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (user.isEmpty())
            return ResponseEntity.badRequest().body("Could not locate your user account");

        LoaderProtJobDto job = loaderProtService.startLoaderCreationJob(user.get().getApiKey(), user.get().getUid());
        logger.info("User started loader protection job, uid={}, jobId={}, sessionId={}",
                user.get().getUid(), job.getId(), request.getSession().getId());
        return ResponseEntity.ok(job.getId());
    }

    @GetMapping("status/{jobId}")
    public ResponseEntity<String> status(HttpServletRequest request, @PathVariable(name = "jobId") String jobId){
        Optional<LoaderProtJobDto> jobDto = loaderProtService.getJobDto(jobId);
        Optional<File> outputFile = loaderProtService.getFinishedJobOutput(jobId);
        if (jobDto.isEmpty())
            return ResponseEntity.badRequest().body("Job id does not exist in our systems");

        // We must check that the user is allowed to access the requested job instance
        Optional<AccountDTO> userDto = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (userDto.isEmpty()){
            logger.warn("Could not find user, sessionId={}", request.getSession().getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user. Please try logging out and back in.");
        }
        else if (!loaderProtService.canUserAccessJob(userDto.get().getUid(), jobDto.get().getId())){
            logger.warn("User attempted to access someone else's loader job status, sessionId={}, user={}",
                    request.getSession().getId(), userDto.get());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user. Please try logging out and back in.");
        }

        if (outputFile.isPresent())
            return ResponseEntity.ok(jobDto.get().getState());
        return ResponseEntity.accepted().body(jobDto.get().getState());
    }

    @GetMapping("download/{jobId}")
    public ResponseEntity<?> download(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @PathVariable(name = "jobId") String jobId) throws IOException {
        Optional<AccountDTO> user = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (user.isEmpty()) {
            response.sendRedirect("/app/error?errorMessage=%s".formatted(URLEncoder.encode("Could not locate your user account", StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().body("Could not locate your user account");
        }

        // Users are only allowed to download their own loader jobs
        if (!loaderProtService.canUserAccessJob(user.get().getUid(), jobId)){
            logger.warn("User attempted to access someone else's loader job status, sessionId={}, user={}",
                    request.getSession().getId(), user.get());
            response.sendRedirect("/app/error?errorMessage=%s".formatted(URLEncoder.encode("Not authorized to access this loader job", StandardCharsets.UTF_8)));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized to access this loader job");
        }

        Optional<File> output = loaderProtService.getFinishedJobOutput(jobId);
        if (output.isEmpty()) {
            response.sendRedirect("/app/error?errorMessage=%s".formatted(URLEncoder.encode("Job id does not exist", StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().body("Job id %s does not exist".formatted(jobId));
        }

        logger.info("User downloaded protected loader, uid={}, sessionId={}, loaderPath={}",
                user.get().getUid(), request.getSession().getId(), output.get().getAbsolutePath());

        Path path = Paths.get(output.get().getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders responseHeaders = new HttpHeaders();
        String userLoaderFileName = downloadedLoaderFileName.formatted(user.get().getUid());
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(userLoaderFileName));
        return ResponseEntity.ok()
                .contentLength(output.get().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(responseHeaders)
                .body(resource);
    }

}
