package williamnogueira.dev.shortener.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
public class UrlEntity {

    private String code;
    @Getter
    private String originalUrl;
    @Getter
    private Long clicks;
    @Getter
    private Instant createdAt;

    @DynamoDbPartitionKey
    public String getCode() {
        return code;
    }
}
