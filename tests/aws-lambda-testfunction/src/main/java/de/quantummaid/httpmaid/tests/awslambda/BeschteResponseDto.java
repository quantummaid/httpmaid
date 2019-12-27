package de.quantummaid.httpmaid.tests.awslambda;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class BeschteResponseDto {
    public final BeschteDomainObject answer;

    public static BeschteResponseDto deserialize(final BeschteDomainObject answer) {
        return new BeschteResponseDto(answer);
    }
}
