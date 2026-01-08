package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.ImageRepositry;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    final ImageRepositry imageRepositry;

    public ImageService(ImageRepositry imageRepositry) {
        this.imageRepositry = imageRepositry;
    }

    public Image getImage(Long id){
        return imageRepositry.findById(id).orElse(null);
    }

    public Image saveImage(Image image){
        return imageRepositry.save(image);
    }

    public void deleteImage(Long id){
        imageRepositry.deleteById(id);
    }

    public Image updateImage(Long id, Image image){
        Image oldImage = imageRepositry.findById(id).orElse(null);
        if(oldImage == null)
            return saveImage(image);
        oldImage.setContentType(image.getContentType());
        oldImage.setData(image.getData());
        return imageRepositry.save(oldImage);
    }
}
