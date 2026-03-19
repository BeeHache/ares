package net.blackhacker.ares.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.blackhacker.ares.projection.AccountProjection;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountProjection.AccountType, String> {
    @Override
    public String convertToDatabaseColumn(AccountProjection.AccountType attribute) {
        return attribute.name();
    }

    @Override
    public AccountProjection.AccountType convertToEntityAttribute(String dbData) {
        return AccountProjection.AccountType.valueOf(dbData);
    }
}
