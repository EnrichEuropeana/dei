package pl.psnc.dei;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.MainView;

@SpringBootApplication(scanBasePackageClasses = {MainView.class, DeiApplication.class}, exclude = ErrorMvcAutoConfiguration.class)
public class DeiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DeiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DeiApplication.class);
    }

    @Bean
    public CommandLineRunner run(TranscriptionPlatformService transcriptionPlatformService) throws Exception {
        return args -> {
            transcriptionPlatformService.refreshAvailableProjects();
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/transcription/iiif/manifest**").allowedOrigins("*");
            }
        };
    }}

