package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UrlRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;

    private static final TableSchema<UrlEntity> SCHEMA = TableSchema.fromBean(UrlEntity.class);

    public void save(UrlEntity entity) {
        getTable().putItem(entity);
    }

    public Optional<UrlEntity> findById(String code) {
        var entity = getTable().getItem(r -> r.key(k -> k.partitionValue(code)));
        return Optional.ofNullable(entity);
    }

    public void updateClickCount(String code, long totalClicks) {
        dynamoDbClient.updateItem(req -> req
                .tableName("urls")
                .key(Map.of("code", AttributeValue.builder().s(code).build()))
                .updateExpression("ADD clicks :inc")
                .expressionAttributeValues(Map.of(":inc", AttributeValue.builder().n(String.valueOf(totalClicks)).build()))
        );
    }

    private DynamoDbTable<UrlEntity> getTable() {
        return enhancedClient.table("urls", SCHEMA);
    }
}
