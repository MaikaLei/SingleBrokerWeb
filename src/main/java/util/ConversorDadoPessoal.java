package util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ConversorDadoPessoal implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String valor) {
        return CriptografiaDados.criptografar(valor);
    }

    @Override
    public String convertToEntityAttribute(String valor) {
        return CriptografiaDados.descriptografar(valor);
    }
}
