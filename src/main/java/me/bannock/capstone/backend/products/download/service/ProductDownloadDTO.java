package me.bannock.capstone.backend.products.download.service;

import java.util.Arrays;

public class ProductDownloadDTO {

    /**
     * @param fileName The name that should be used if the user were to download the executable
     * @param bytes The bytes of the executable
     */
    public ProductDownloadDTO(String fileName, byte... bytes){
        this.fileName = fileName;
        this.bytes = bytes;
    }

    private final String fileName;
    private final byte[] bytes;

    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "ProductDownloadDTO{" +
                "fileName='" + fileName + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }

}
