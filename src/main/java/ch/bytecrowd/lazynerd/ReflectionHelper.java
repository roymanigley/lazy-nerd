package ch.bytecrowd.lazynerd;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ReflectionHelper {

    private ReflectionHelper() {

    }

    public static HashMap<String, Function<Object, Object>> getFieldsMap(Class<?> clazz) {
        var fields = Arrays.asList(clazz.getDeclaredFields());
        var fieldsMap = new HashMap<String, Function<Object, Object>>();
        fields.forEach(field -> fieldsMap.put(field.getName(), o -> {
            try {
                field.setAccessible(true);
                return field.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));
        return fieldsMap;
    }

    public static String getParentPackageName(Class clazz) {
        var entityPackageName = clazz.getPackageName();
        var basePackage = entityPackageName.substring(0, entityPackageName.lastIndexOf("."));
        return entityPackageName.equals(basePackage) ? "" : basePackage;
    }

    public static Optional<Class> getIdType(Class clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(ReflectionHelper::isAnnotatedWithId)
                .findFirst()
                .map(field -> field.getType());
    }

    public static boolean isAnnotatedWithId(Field field) {
        return isAnnotatedWith(field, typeName -> "javax.persistence.Id".equals(typeName));
    }

    public static boolean isAnnotatedWithManyToOne(Field field) {
        return isAnnotatedWith(field, typeName -> "javax.persistence.ManyToOne".equals(typeName));
    }

    public static boolean isAnnotatedWithOneToMany(Field field) {
        return isAnnotatedWith(field, typeName -> "javax.persistence.OneToMany".equals(typeName));
    }

    public static boolean isAnnotatedWith(Field field, Predicate<String> filter) {
        return Arrays.stream(field.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .map(Class::getName)
                .anyMatch(filter);
    }
}
