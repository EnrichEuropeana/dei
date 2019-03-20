package pl.psnc.dei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import pl.psnc.dei.ui.MainView;

@SpringBootApplication(scanBasePackageClasses = { MainView.class, DeiApplication.class }, exclude = ErrorMvcAutoConfiguration.class)
public class DeiApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(DeiApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DeiApplication.class);
	}
}

