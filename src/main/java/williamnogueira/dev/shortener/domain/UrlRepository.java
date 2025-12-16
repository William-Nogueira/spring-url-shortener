package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UrlRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    private static final TableSchema<UrlEntity> SCHEMA = TableSchema.fromBean(UrlEntity.class);

    public void save(UrlEntity entity) {
        getTable().putItem(entity);
    }

    public Optional<UrlEntity> findById(String code) {
        var entity = getTable().getItem(r -> r.key(k -> k.partitionValue(code)));
        return Optional.ofNullable(entity);
    }

    private DynamoDbTable<UrlEntity> getTable() {
        return enhancedClient.table("urls", SCHEMA);
    }
}
