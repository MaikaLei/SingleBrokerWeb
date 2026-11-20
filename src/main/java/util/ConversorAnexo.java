package util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ConversorAnexo implements AttributeConverter<byte[], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(byte[] valor) {
        return CriptografiaDados.criptografar(valor);
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] valor) {
        return CriptografiaDados.descriptografar(valor);
    }
}
