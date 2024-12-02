/*
import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotenvConfig {
    @Bean
    public CommandLineRunner loadEnvProperties() {
        return args -> {
            Dotenv dotenv = Dotenv.configure().load();
            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
            System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
            System.setProperty("API_KEY", dotenv.get("API_KEY"));
            System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
        };
    }
}
*/
