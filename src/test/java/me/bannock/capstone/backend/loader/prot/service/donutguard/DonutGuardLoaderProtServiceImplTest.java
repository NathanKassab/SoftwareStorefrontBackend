package me.bannock.capstone.backend.loader.prot.service.donutguard;

import me.bannock.capstone.backend.loader.prot.service.LoaderProtJobDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DonutGuardLoaderProtServiceImplTest {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private DonutGuardLoaderProtServiceImpl loaderProtService;

    private static final String TEST_API_KEY = "TEST";
    private static final long TEST_UID = 0;

    @Test
    void startLoaderCreationJob() {
        LoaderProtJobDto dto = loaderProtService.startLoaderCreationJob(TEST_API_KEY, TEST_UID);
        Optional<File> output;
        do{
            output = loaderProtService.getFinishedJobOutput(dto.getId());
            try{
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            logger.info("Current state of test job: {}", loaderProtService.getJobStatus(dto.getId()));
        }while(output.isEmpty());
        assertTrue(output.get().exists());
    }

}