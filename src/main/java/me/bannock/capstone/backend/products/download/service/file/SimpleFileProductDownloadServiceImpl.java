package me.bannock.capstone.backend.products.download.service.file;

import me.bannock.capstone.backend.products.download.service.ProductDownloadDTO;
import me.bannock.capstone.backend.products.download.service.ProductDownloadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
public class SimpleFileProductDownloadServiceImpl implements ProductDownloadService {

    private final Logger logger = LogManager.getLogger();

    @Value("${backend.productDownloadService.file.downloadDir}")
    private String downloadDir;

    @Override
    public Optional<ProductDownloadDTO> getProductDownload(long productId) {
        String fileName = "%s.jar".formatted(productId);
        File download = new File(downloadDir, fileName);
        if (!download.exists())
            return Optional.empty();

        byte[] productBytes;
        try {
            productBytes = Files.readAllBytes(download.toPath());
        } catch (IOException e) {
            logger.error("Something went wrong while reading a product download's bytes, productId={}, downloadPath={}, error={}",
                    productId, download.getAbsolutePath(), e);
            return Optional.empty();
        }

        return Optional.of(new ProductDownloadDTO(fileName, productBytes));
    }

}
