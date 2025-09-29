package com.music.musicstore.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-cache-seconds:0}")
    private int cacheSeconds;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL path /uploads/music/** to the filesystem uploads directory configured in application.properties
        // Resolve configured upload directory (can be a relative path like src/main/resources/static/uploads/music)
        Path fsUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String filesystemLocation = fsUploadPath.toUri().toString(); // e.g. file:///...

        // Also include classpath location as a fallback (useful when serving packaged/static resources)
        String classpathMusicLocation = "classpath:/static/uploads/music/";

        // Additional common runtime locations to support both development and packaged runs.
        // Some environments store uploaded files in ./uploads/music at runtime; include that as a fallback.
        Path runtimeUploads = Paths.get("./uploads/music").toAbsolutePath().normalize();
        String runtimeUploadsLocation = runtimeUploads.toUri().toString();

        // Also include the built classes static folder (useful during development when resources are copied to target/)
        Path targetClassesUploads = Paths.get("target/classes/static/uploads/music").toAbsolutePath().normalize();
        String targetClassesLocation = targetClassesUploads.toUri().toString();

        registry.addResourceHandler("/uploads/music/**")
            .addResourceLocations(filesystemLocation, runtimeUploadsLocation, targetClassesLocation, classpathMusicLocation)
            .setCachePeriod(cacheSeconds);

        // -- covers handler --
        // Compute a filesystem location that points to the 'covers' sibling of the configured 'music' folder
        String filesystemCoversLocation;
        try {
            Path coversPath;
            Path fileName = fsUploadPath.getFileName();
            if (fileName != null && fileName.toString().equalsIgnoreCase("music")) {
                Path parent = fsUploadPath.getParent();
                if (parent != null) {
                    coversPath = parent.resolve("covers").toAbsolutePath().normalize();
                } else {
                    coversPath = fsUploadPath.resolveSibling("covers").toAbsolutePath().normalize();
                }
            } else {
                coversPath = fsUploadPath.resolve("covers").toAbsolutePath().normalize();
            }
            filesystemCoversLocation = coversPath.toUri().toString();
        } catch (Exception ex) {
            filesystemCoversLocation = Paths.get("./uploads/covers").toAbsolutePath().normalize().toUri().toString();
        }

        String classpathCoversLocation = "classpath:/static/uploads/covers/";
        String runtimeCoversLocation = Paths.get("./uploads/covers").toAbsolutePath().normalize().toUri().toString();
        String targetClassesCoversLocation = Paths.get("target/classes/static/uploads/covers").toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/uploads/covers/**")
            .addResourceLocations(filesystemCoversLocation, runtimeCoversLocation, targetClassesCoversLocation, classpathCoversLocation)
            .setCachePeriod(cacheSeconds);
    }
}
