package ch.bytecrowd.lazynerd;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;

@FunctionalInterface
public interface ParamProvider {

    static Map<String, Object> paramsFromEntityField(Class entity, Field field) {
        var genericTypeCanonical = "";
        var genericType = "";
        var genericTypeVariable = "";
        if (field.getGenericType() instanceof ParameterizedType parameterizedType && parameterizedType.getActualTypeArguments().length > 0) {
            genericTypeCanonical = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName();
            genericType = genericTypeCanonical.substring(genericTypeCanonical.lastIndexOf(".") + 1);
            genericTypeVariable = genericType.substring(0, 1).toLowerCase() + genericType.substring(1);
        }

        return Map.of(
                "entityTypeSimpleName", entity.getSimpleName(),
                "fieldType", field.getType().getSimpleName() + (genericType.isBlank() ? "" : "<" + genericType + ">"),
                "fieldName", field.getName(),
                "fieldNamePascalCase", field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
                "fieldNameUpperCase", field.getName().replaceAll("(.)([A-Z])", "$1_$2").toUpperCase(),
                "genericType", genericType,
                "genericTypeVariable", genericTypeVariable
        );
    }

    record EntityTypeField(
            String entityTypeSimpleName,
            String fieldType,
            String fieldName,
            String fieldNamePascalCase,
            String fieldNameUpperCase,
            String genericType,
            String genericTypeVariable
    ) {
        static EntityTypeField fromMap(Map<String, Object> map) {
            return new EntityTypeField(
                    map.get("entityTypeSimpleName") + "",
                    map.get("fieldType") + "",
                    map.get("fieldName") + "",
                    map.get("fieldNamePascalCase") + "",
                    map.get("fieldNameUpperCase") + "",
                    map.get("genericType") + "",
                    map.get("genericTypeVariable") + ""
            );
        }
    }

    static Map<String, Object> paramsFromEntity(Class clazz) {
        var basePackage = ReflectionHelper.getParentPackageName(clazz);
        var entityTypeSimpleName = clazz.getSimpleName();
        var entityTypeCanonicalName = clazz.getName();
        var idTypeSimpleName = ReflectionHelper.getIdType(clazz)
                .map(Class::getSimpleName)
                .orElse(Integer.class.getSimpleName());
        var idTypeCanonicalName = ReflectionHelper.getIdType(clazz)
                .map(Class::getName)
                .orElse(Integer.class.getName());
        var entityRestResourceName = entityTypeSimpleName.replaceAll("(.)([A-Z])", "$1-$2").toLowerCase();
        var entityTypeFields = Arrays.stream(clazz.getDeclaredFields())
                .map(field -> paramsFromEntityField(clazz, field))
                .map(EntityTypeField::fromMap)
                .toList();

        return Map.of(
                "basePackage", basePackage,
                "entityTypeSimpleName", entityTypeSimpleName,
                "entityTypeCanonicalName", entityTypeCanonicalName,
                "entityTypeVariableName", entityTypeSimpleName.substring(0, 1).toLowerCase() + entityTypeSimpleName.substring(1),
                "idTypeSimpleName", idTypeSimpleName,
                "idTypeCanonicalName", idTypeCanonicalName,
                "entityRestResourceName", entityRestResourceName,
                "entityTypeFields", entityTypeFields
        );
    }

    Map<String, Object> provideParams();
}
