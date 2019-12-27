package de.quantummaid.httpmaid.tests.awslambda;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class BeschteRequestDto {
    public final BeschteDomainObject question;

    public static BeschteRequestDto deserialize(final BeschteDomainObject question) {
        return new BeschteRequestDto(question);
    }
}
