package cl.cappucare.backend;
import org.springframework.context.annotation.Configuration;import org.springframework.web.servlet.config.annotation.*;
@Configuration public class UploadConfig implements WebMvcConfigurer {public void addResourceHandlers(ResourceHandlerRegistry r){r.addResourceHandler("/uploads/**").addResourceLocations("file:./uploads/");}}
