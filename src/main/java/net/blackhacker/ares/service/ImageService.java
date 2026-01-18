package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.ImageRepositry;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ImageService {

    final ImageRepositry imageRepositry;

    public ImageService(ImageRepositry imageRepositry) {
        this.imageRepositry = imageRepositry;
    }

    public Optional<Image> getImage(Long id){
        return imageRepositry.findById(id);
    }

    public Image saveImage(Image image){
        return imageRepositry.save(image);
    }

    public void deleteImage(Long id){
        imageRepositry.deleteById(id);
    }

    public Image updateImage(Long id, Image image){
        Optional<Image> oldImageOption = imageRepositry.findById(id);
        if(oldImageOption.isPresent()) {
            Image oldImage = oldImageOption.get();
            oldImage.setData(image.getData());
            oldImage.setContentType(image.getContentType());
            return imageRepositry.save(oldImage);
        }

        return imageRepositry.save(image);
    }
}
