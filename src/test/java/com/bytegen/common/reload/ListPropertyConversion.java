package com.bytegen.common.reload;

import com.bytegen.common.reload.conversion.PropertyConversion;
import com.google.common.base.Function;
import org.springframework.beans.factory.BeanInitializationException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ListPropertyConversion implements PropertyConversion {


    @Override
    public Object convertPropertyForField(final Field field, final Object property) {
        try {
            return new ListConverter(field.getType()).apply(property);
        } catch (final Throwable e) {
            throw new BeanInitializationException(
                    String.format("Unable to convert property for field [%s].  Value [%s] cannot be converted to [%s]",
                            field.getName(), property, field.getType()), e);
        }
    }

    private static class ListConverter implements Function<Object, List> {
        private final Class<?> type;

        public ListConverter(final Class<?> type) {
            this.type = type;
        }

        @Override
        public List apply(final Object input) {
            if (null == input) {
                return null;
            }
            return Arrays.asList(input.toString().split(";"));
        }
    }
}

