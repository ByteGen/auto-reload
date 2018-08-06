package com.bytegen.common.reload.conversion;

import java.lang.reflect.Field;

/**
 * Interface intended for use by any class willing to convert the given property {@link Object} which potentially requires conversion before being set on the
 * given {@link Field}
 */
public interface PropertyConversion {

    /**
     * @param field         the destination filed to set the property on
     * @param propertyValue the property to be converted for the given field
     * @return the potentially converted field
     */
    Object convertPropertyForField(final Field field, final Object propertyValue);
}
