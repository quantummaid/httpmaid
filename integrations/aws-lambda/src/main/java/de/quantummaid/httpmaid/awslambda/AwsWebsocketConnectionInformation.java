package de.quantummaid.httpmaid.awslambda;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.httpmaid.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsWebsocketConnectionInformation {
    private final String connectionId;
    private final String stage;
    private final String apiId;
    private final String region;
    
    public static AwsWebsocketConnectionInformation awsWebsocketConnectionInformation(final String connectionId,
                                                                                      final String stage,
                                                                                      final String apiId,
                                                                                      final String region) {
        validateNotNull(connectionId, "connectionId");
        validateNotNull(stage, "stage");
        validateNotNull(apiId, "apiId");
        validateNotNull(region, "region");
        return new AwsWebsocketConnectionInformation(connectionId, stage, apiId, region);
    }

    public String toEndpointUrl() {
        final String domainName = String.format("%s.execute-api.%s.amazonaws.com", apiId, region);
        return String.format("https://%s/%s", domainName, stage);
    }

    public String region() {
        return region;
    }

    public String connectionId() {
        return connectionId;
    }
}
