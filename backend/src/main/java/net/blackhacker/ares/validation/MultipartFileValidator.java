package net.blackhacker.ares.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MultipartFileValidator {
        public void validateMultipartFile(MultipartFile file){
            if (file == null || file.isEmpty()){
                throw new ValidationException("File is empty");
            }
        }
}
