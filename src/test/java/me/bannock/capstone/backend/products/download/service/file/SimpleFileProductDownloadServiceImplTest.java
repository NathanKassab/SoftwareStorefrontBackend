package me.bannock.capstone.backend.products.download.service.file;

import me.bannock.capstone.backend.products.download.service.ProductDownloadDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SimpleFileProductDownloadServiceImplTest {

    private final long TESTING_PRODUCT_ID = 0;

    @Autowired
    private SimpleFileProductDownloadServiceImpl productDownloadService;

    @Test
    void getProductBytes(){
        Optional<ProductDownloadDTO> downloadDto = productDownloadService.getProductDownload(TESTING_PRODUCT_ID);
        assertTrue(downloadDto.isPresent());
    }

}