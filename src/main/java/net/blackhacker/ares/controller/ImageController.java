package net.blackhacker.ares.controller;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(net.blackhacker.ares.service.ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable("id") Long id) {
        Image image = imageService.getImage(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = MediaType.parseMediaType(image.getContentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(image.getData());
    }

}
