package onetoone.WorkoutExercises;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this class as a configuration class
public class ApiNinjaConfig {

    @Value("${api.ninja.key}") // Injects the API key from application.properties
    private String apiKey;

    @Value("${api.ninja.url}") // Injects the API URL from application.properties
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}