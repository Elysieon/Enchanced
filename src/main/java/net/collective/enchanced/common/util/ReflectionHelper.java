package net.collective.enchanced.common.util;

import java.lang.reflect.Field;

public interface ReflectionHelper {
    @SuppressWarnings("unchecked")
    static <U, T> T getFieldValue(Class<U> clazz, U object, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}
