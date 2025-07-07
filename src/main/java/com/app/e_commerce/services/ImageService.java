package com.app.e_commerce.services;

import com.app.e_commerce.config.ImageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class ImageService {

    private final ImageProperties imageProperties;
    private static final String IMAGE_CACHE_NAME = "imageCache";

    public ImageService(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    /**
     * Converts and optimizes an image file to Base64 with caching
     */

    @Cacheable(value = IMAGE_CACHE_NAME, key = "#file.originalFilename")
    public String convertToBase64(MultipartFile file) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage optimizedImage = optimizeImage(originalImage);
            return encodeToBase64(optimizedImage, getImageFormat(file.getContentType()));
        } catch (IOException e) {
            log.error("Failed to convert image to Base64", e);
            throw new IOException("Failed to process image", e);
        }
    }

    /**
     * Gets default avatar as Base64 with caching
     */
    @Cacheable(value = IMAGE_CACHE_NAME, key = "'defaultAvatar'")
    public String getDefaultAvatarBase64() throws IOException {
        try {
            ClassPathResource imgFile = new ClassPathResource(imageProperties.getDefaultAvatarPath());
            BufferedImage originalImage = ImageIO.read(imgFile.getInputStream());
            BufferedImage optimizedImage = optimizeImage(originalImage);
            return encodeToBase64(optimizedImage, "png");
        } catch (IOException e) {
            log.error("Failed to load default avatar", e);
            throw new IOException("Failed to load default avatar", e);
        }
    }

    /**
     * Optimizes an image by resizing and compressing it
     */
    private BufferedImage optimizeImage(BufferedImage original) {
        // Resize image if it's larger than maximum dimensions
        if (original.getWidth() > imageProperties.getMaxWidth() ||
                original.getHeight() > imageProperties.getMaxHeight()) {
            return resizeImage(original);
        }
        return original;
    }

    /**
     * Resizes the image maintaining aspect ratio
     */
    private BufferedImage resizeImage(BufferedImage original) {
        double aspectRatio = (double) original.getWidth() / original.getHeight();
        int targetWidth = imageProperties.getMaxWidth();
        int targetHeight = imageProperties.getMaxHeight();

        if (aspectRatio > 1) {
            targetHeight = (int) (targetWidth / aspectRatio);
        } else {
            targetWidth = (int) (targetHeight * aspectRatio);
        }

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();

        try {
            // Use better quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
            return resized;
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Encodes image to Base64 with compression
     */
    private String encodeToBase64(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Get image writer for the format
            ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            // Set compression if supported
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(imageProperties.getCompressionQuality());
            }

            // Write the image
            try (ByteArrayOutputStream compressedStream = new ByteArrayOutputStream()) {
                writer.setOutput(ImageIO.createImageOutputStream(compressedStream));
                writer.write(null, new IIOImage(image, null, null), writeParam);
                writer.dispose();

                // Convert to Base64
                byte[] imageBytes = compressedStream.toByteArray();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        }
    }

    /**
     * Validates and returns image format from content type
     */
    private String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpeg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpeg";
        };
    }

    /**
     * Validates Base64 string and converts it back to BufferedImage
     */
    public BufferedImage validateAndDecodeBase64(String base64Image) throws IOException {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                BufferedImage image = ImageIO.read(bis);
                if (image == null) {
                    throw new IOException("Invalid image data");
                }
                return image;
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid Base64 string", e);
        }
    }
}