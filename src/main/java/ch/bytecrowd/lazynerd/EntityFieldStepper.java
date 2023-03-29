package ch.bytecrowd.lazynerd;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

public class EntityFieldStepper {

    public static void forEachFieldOf(Class clazz, Consumer<Field> fieldHandler) {
        Arrays.stream(clazz.getDeclaredFields())
                .forEach(fieldHandler);
    }
    public static String generateEntityBoilerPlate(Class clazz) {
        var builder = new StringBuilder();
        forEachFieldOf(clazz, field -> {
            builder.append(
                    generateGetterAndSetterForField(clazz, field)
            );
            if (ReflectionHelper.isAnnotatedWithOneToMany(field)) {
                builder.append(
                        generateAddMethodForOneToManyField(clazz, field)
                );
            }
        });
        builder.append(
                generateEqualsAndHashCode(clazz)
        );
        return builder.toString();
    }

    static String generateGetterAndSetterForField(Class clazz, Field field) {
        return new TemplateFiller().fillUpTemplate(
                """
                            public ${fieldType} get${fieldNamePascalCase}() {
                                return ${fieldName};
                            }
                                                                   
                            public void set${fieldNamePascalCase}(${fieldType} ${fieldName}) {
                                this.${fieldName} = ${fieldName};
                            }
                                                                
                            public ${entityTypeSimpleName} ${fieldName}(${fieldType} ${fieldName}) {
                                set${fieldNamePascalCase}(${fieldName});
                                return this;
                            }
                            
                        """,
                () -> ParamProvider.paramsFromEntityField(clazz, field)
        );
    }

    static String generateAddMethodForOneToManyField(Class clazz, Field field) {
        return new TemplateFiller().fillUpTemplate(
                """                                                              
                            public ${entityTypeSimpleName} addTo${fieldNamePascalCase}(${genericType} ${genericTypeVariable}) {
                                ${fieldName}.add(${genericTypeVariable});
                                return this;
                            }
                            
                        """,
                () -> ParamProvider.paramsFromEntityField(clazz, field)
        );
    }

    static String generateEqualsAndHashCode(Class clazz) {
        return ReflectionHelper.getIdType(clazz)
                .map(idType -> new TemplateFiller().fillUpTemplate(
                        """                                       
                                    @Override
                                    public boolean equals(Object o) {
                                        if (this == o) return true;
                                        if (o == null || getClass() != o.getClass()) return false;
                                        ${entityTypeSimpleName} entity = (${entityTypeSimpleName}) o;
                                        return Objects.equals(id, entity.id);
                                    }
                                                
                                    /**
                                    https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
                                    */
                                    @Override
                                    public int hashCode() {
                                        return getClass().hashCode();
                                    }
                                """,
                        () -> Map.of(
                                "entityTypeSimpleName", clazz.getSimpleName(),
                                "idType", idType.getSimpleName()
                        )
                )).orElse("");
    }
}
