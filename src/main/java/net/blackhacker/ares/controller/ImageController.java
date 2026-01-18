package net.blackhacker.ares.controller;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(net.blackhacker.ares.service.ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        Optional<Image> image = imageService.getImage(id);
        if (image.isEmpty()) {
            throw new ControllerException(HttpStatus.NOT_FOUND, "Image not found");
        }

        MediaType contentType = MediaType.parseMediaType(image.get().getContentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(image.get().getData());
    }

}
