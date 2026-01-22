package net.blackhacker.ares.controller;


import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import net.blackhacker.ares.service.ImageService;
import net.blackhacker.ares.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler forbiddenHandler;

    @Test
    void getImage_shouldReturnImage_whenImageExists() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        byte[] imageData = new byte[]{1, 2, 3};
        String contentType = "image/png";

        Image image = new Image();
        image.setId(id);
        image.setData(imageData);
        image.setContentType(contentType);

        when(imageService.getImage(id)).thenReturn(Optional.of(image));

        // Act & Assert
        mockMvc.perform(get("/api/image/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageData));
    }

    @Test
    void getImage_shouldReturnNotFound_whenImageDoesNotExist() throws Exception {
        // Arrange
        when(imageService.getImage(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/image/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
