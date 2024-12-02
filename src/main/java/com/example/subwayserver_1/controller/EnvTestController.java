import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // 허용할 도메인 명시

public class EnvTestController {

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Value("${API_KEY}")
    private String apiKey;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @GetMapping("/env-test")
    public String testEnv() {
        return String.format("DB_PASSWORD: %s, API_KEY: %s, JWT_SECRET: %s", dbPassword, apiKey, jwtSecret);
    }
}
