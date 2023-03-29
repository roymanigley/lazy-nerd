package ch.bytecrowd.lazynerd;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public final class ReflectionHelper {

    private ReflectionHelper() {

    }

    public static String getParentPackageName(Class clazz) {
        String entityPackageName = clazz.getPackageName();
        String basePackage = entityPackageName.substring(0, entityPackageName.lastIndexOf("."));
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
