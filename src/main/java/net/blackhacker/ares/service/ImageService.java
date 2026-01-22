package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepositry) {
        this.imageRepository = imageRepositry;
    }

    public Optional<Image> getImage(UUID id){
        return imageRepository.findById(id);
    }

    public Image saveImage(Image image){
        return imageRepository.save(image);
    }

    public void deleteImage(UUID id){
        imageRepository.deleteById(id);
    }

    public Image updateImage(UUID id, Image image){
        Optional<Image> oldImageOption = imageRepository.findById(id);
        if(oldImageOption.isPresent()) {
            Image oldImage = oldImageOption.get();
            oldImage.setData(image.getData());
            oldImage.setContentType(image.getContentType());
            return imageRepository.save(oldImage);
        }

        return imageRepository.save(image);
    }
}
