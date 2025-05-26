package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class ImageController {

    // replace this! careful with the operating system in use
    private static String directory = "C:/Users/ajpa0/Downloads";

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping(value = "/images/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable int id) throws IOException {
        Image image = imageRepository.findById(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        File imageFile = new File(image.getFilePath());
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        // Determine the media type based on file extension
        String contentType = determineContentType(imageFile.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageBytes);
    }

    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            // Default to octet-stream if type is unknown
            return "application/octet-stream";
        }
    }

    @PostMapping("images")
    public String handleFileUpload(@RequestParam("image") MultipartFile imageFile)  {
        // Check if the file is a valid image type
        String contentType = imageFile.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"))) {

        }

        try {
            File destinationFile = new File(directory + File.separator + imageFile.getOriginalFilename());
            imageFile.transferTo(destinationFile);  // save file to disk

            Image image = new Image();
            image.setFilePath(destinationFile.getAbsolutePath());
            imageRepository.save(image);

            return "File uploaded successfully: " + destinationFile.getAbsolutePath();
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        }
    }

}
