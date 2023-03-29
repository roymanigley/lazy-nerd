package ch.bytecrowd.lazynerd;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

@FunctionalInterface
public interface ParamProvider {

    static Map<String, String> paramsFromEntityField(Class entity, Field field) {
        String genericTypeCanonical = "";
        String genericType = "";
        String genericTypeVariable = "";
        if (field.getGenericType() instanceof ParameterizedType parameterizedType && parameterizedType.getActualTypeArguments().length > 0) {
            genericTypeCanonical = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName();
            genericType = genericTypeCanonical.substring(genericTypeCanonical.lastIndexOf(".") + 1);
            genericTypeVariable = genericType.substring(0, 1).toLowerCase() + genericType.substring(1);
        }

        if (!genericType.isBlank()) {

        }
        return Map.of(
                "entityTypeSimpleName", entity.getSimpleName(),
                "fieldType", field.getType().getSimpleName() + (genericType.isBlank() ? "" : "<" + genericType + ">"),
                "fieldName", field.getName(),
                "fieldNamePascalCase", field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
                "genericType", genericType,
                "genericTypeVariable", genericTypeVariable
        );
    }

    static Map<String, String> paramsFromEntity(Class clazz) {
        String basePackage = ReflectionHelper.getParentPackageName(clazz);
        String entityTypeSimpleName = clazz.getSimpleName();
        String entityTypeCanonicalName = clazz.getName();
        String idTypeSimpleName = ReflectionHelper.getIdType(clazz)
                .map(Class::getSimpleName)
                .orElse(Integer.class.getSimpleName());
        String idTypeCanonicalName = ReflectionHelper.getIdType(clazz)
                .map(Class::getName)
                .orElse(Integer.class.getName());
        String entityRestResourceName = entityTypeSimpleName.replaceAll("(.)([A-Z])", "$1-$2").toLowerCase();

        var params = Map.of(
                "basePackage", basePackage,
                "entityTypeSimpleName", entityTypeSimpleName,
                "entityTypeCanonicalName", entityTypeCanonicalName,
                "entityTypeVariableName", entityTypeSimpleName.substring(0, 1).toLowerCase() + entityTypeSimpleName.substring(1),
                "idTypeSimpleName", idTypeSimpleName,
                "idTypeCanonicalName", idTypeCanonicalName,
                "entityRestResourceName", entityRestResourceName
        );
        return params;
    }

    Map<String, String> provideParams();
}
