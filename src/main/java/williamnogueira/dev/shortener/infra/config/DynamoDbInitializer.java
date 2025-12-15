package williamnogueira.dev.shortener.infra.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import williamnogueira.dev.shortener.domain.UrlEntity;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DynamoDbInitializer {

    private final DynamoDbEnhancedClient enhancedClient;

    private static final String TABLE = "urls";

    @PostConstruct
    public void setupDatabase() {
        try {
            var table = enhancedClient.table(TABLE, TableSchema.fromBean(UrlEntity.class));
            table.createTable();
        } catch (ResourceInUseException _) {
            log.info("Table '{}' already exists. Skipping creation.", TABLE);
        }
    }
}
