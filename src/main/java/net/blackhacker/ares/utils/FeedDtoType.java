package net.blackhacker.ares.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.blackhacker.ares.dto.FeedDTO;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

@Component
public class FeedDtoType implements UserType<FeedDTO> {


    private final ObjectMapper objectMapper;

    public FeedDtoType(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int getSqlType() {
        return Types.JAVA_OBJECT; // Or Types.OTHER for JSONB
    }

    @Override
    public Class<FeedDTO> returnedClass() {
        return FeedDTO.class;
    }

    @Override
    public boolean equals(FeedDTO x, FeedDTO y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(FeedDTO x) {
        return Objects.hashCode(x);
    }

    @Override
    public FeedDTO nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String json = rs.getString(position);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, FeedDTO.class);
        } catch (IOException e) {
            throw new HibernateException("Failed to convert JSON to FeedDTO", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, FeedDTO value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        try {
            st.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new HibernateException("Failed to convert FeedDTO to JSON", e);
        }
    }

    @Override
    public FeedDTO deepCopy(FeedDTO value) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(value), FeedDTO.class);
        } catch (IOException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(FeedDTO value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public FeedDTO assemble(Serializable cached, Object owner) {
        try {
            return objectMapper.readValue((String) cached, FeedDTO.class);
        } catch (IOException e) {
            throw new HibernateException(e);
        }
    }
}
