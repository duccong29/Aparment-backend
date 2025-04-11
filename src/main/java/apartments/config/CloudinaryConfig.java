package apartments.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        final Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dgvylsy5z");
        config.put("api_key", "635935476999317");
        config.put("api_secret", "PX6X7nvRO7tQjA2i4FXbPYmkXgM");
        return new Cloudinary(config);
    }
}
