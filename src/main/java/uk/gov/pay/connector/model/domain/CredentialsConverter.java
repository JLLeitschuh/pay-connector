package uk.gov.pay.connector.model.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@Converter
public class CredentialsConverter implements AttributeConverter<Map<String,String>, PGobject> {
    @Override
    public PGobject convertToDatabaseColumn(Map<String,String> credentials) {
        PGobject pgCredentials = new PGobject();
        pgCredentials.setType("json");
        try {
            pgCredentials.setValue(credentials.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pgCredentials;
    }

    @Override
    public Map<String,String> convertToEntityAttribute(PGobject dbCredentials) {
        try {
            return new ObjectMapper().readValue(dbCredentials.toString(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
