package de.quantummaid.httpmaid.tests.awslambda;

public class BeschteUseCase {

    public BeschteResponseDto answer(final BeschteRequestDto beschteRequestDto) {
        final BeschteDomainObject question = beschteRequestDto.question;
        return BeschteResponseDto.deserialize(question);
    }

}
