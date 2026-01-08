package net.blackhacker.ares.service;

import org.springframework.stereotype.Service;

@Service
public class URLFetchService {

    public byte[] fetchImageBytes(String imageUrl) throws ServiceException {
        try {
            java.net.URL url = new java.net.URI(imageUrl).toURL();
            java.net.URLConnection connection = url.openConnection();
            try (java.io.InputStream is = connection.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch image from URL: " + imageUrl, e);
        }
    }

    public String getContentType(String imageUrl) throws ServiceException {
        try {
            java.net.URL url = new java.net.URI(imageUrl).toURL();
            java.net.URLConnection connection = url.openConnection();
            return connection.getContentType();
        } catch (Exception e) {
            throw new ServiceException("Failed to get content type from URL: " + imageUrl, e);
        }
    }


}
