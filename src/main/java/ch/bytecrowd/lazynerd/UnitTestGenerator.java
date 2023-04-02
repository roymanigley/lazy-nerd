package ch.bytecrowd.lazynerd;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class UnitTestGenerator {
    private final Class clazz;
    private final String methodRegex;
    private final String sourcePath;
    private final boolean generateTestsForIfStatements;

    public static class Builder {
        private Class clazz;
        private String methodRegex = "^.+";
        private String sourcePath = "src/main/java";
        private boolean generateTestsForIfStatements = true;

        public Builder clazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder methodRegex(String methodRegex) {
            this.methodRegex = methodRegex;
            return this;
        }

        public Builder sourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder generateTestsForIfStatements(boolean generateTestsForIfStatements) {
            this.generateTestsForIfStatements = generateTestsForIfStatements;
            return this;
        }

        public UnitTestGenerator build() {
            if (clazz == null) {
                throw new IllegalArgumentException("clazz can not be null");
            }
            return new UnitTestGenerator(clazz, methodRegex, sourcePath, generateTestsForIfStatements);
        }
    }

    private UnitTestGenerator(Class clazz, String methodRegex, String sourcePath, boolean generateTestsForIfStatements) {
        this.clazz = clazz;
        this.methodRegex = methodRegex;
        this.sourcePath = sourcePath;
        this.generateTestsForIfStatements = generateTestsForIfStatements;
    }

    public String generate() {
        var sourceRoot = new SourceRoot(Paths.get(sourcePath));
        var parsed = sourceRoot.parse(
                clazz.getPackageName(),
                clazz.getSimpleName() + ".java"
        );

        var builder = new StringBuilder();

        builder.append("package " + clazz.getPackageName() + ";\n");
        builder.append("\n");
        builder.append("""
                import org.junit.jupiter.api.BeforeEach;
                import org.junit.jupiter.api.Test;
                import org.mockito.junit.jupiter.MockitoExtension;
                import org.junit.jupiter.api.extension.ExtendWith;
                import org.mockito.Mock;
                                
                import static org.assertj.core.api.Assertions.*;
                import static org.mockito.Mockito.*;
                """);
        builder.append("\n");

        builder.append("@ExtendWith(MockitoExtension.class)\n");
        builder.append("class " + clazz.getSimpleName() + "Test {\n");
        builder.append("\n");
        List<String> mocks = new ArrayList<>();
        List<ConstructorDeclaration> constructors = parsed.findAll(ConstructorDeclaration.class);
        constructors.stream()
                .filter(constructorDeclaration -> !constructorDeclaration.hasModifier(Modifier.Keyword.PRIVATE))
                .map(CallableDeclaration::getParameters)
                .flatMap(Collection::stream)
                .distinct()
                .forEach(parameter -> {
                    ClassOrInterfaceType type = (ClassOrInterfaceType) parameter.getType();
                    if (type.isPrimitiveType() || type.isBoxedType()){
                        builder.append("    " + parameter + " = 42;\n");
                    } else if (type.getNameAsString().equals("String")) {
                        builder.append("    " + parameter + " = \"42\";\n");
                    } else {
                        builder.append("    @Mock\n");
                        builder.append("    " + parameter +";\n");
                        mocks.add(parameter.getNameAsString());
                    }
                });

        builder.append("\n");

        builder.append("    " + clazz.getSimpleName() + " target;\n");
        builder.append("\n");
        builder.append("    @BeforeEach\n");
        builder.append("    void init() {\n");
        constructors.stream()
                .filter(constructorDeclaration -> !constructorDeclaration.hasModifier(Modifier.Keyword.PRIVATE))
                .forEach(constructorDeclaration -> {
                    String arguments = constructorDeclaration.getParameters()
                            .stream().map(Parameter::getNameAsString)
                            .collect(Collectors.joining(", "));

                    builder.append("        target = new " + clazz.getSimpleName() + "(" + arguments +");\n");
                });
        if (constructors.isEmpty()) {
            builder.append("        target = new " + clazz.getSimpleName() + "();\n");
        }
        builder.append("    }\n");
        builder.append("\n");

        parsed.findAll(MethodDeclaration.class).stream()
                .filter(declaration -> !declaration.hasModifier(Modifier.Keyword.PRIVATE))
                .filter(declaration -> declaration.getNameAsString().matches(methodRegex))
                .forEach(method -> {

            var methodName = method.getNameAsString();
            var parameters = method.getParameters();
            var methodNamePascalCase = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
            var methodInvocation = getMethodInvocation(methodName, parameters);
            var arguments = parameters.stream().map(param ->
                    "        " + generateMockedVariable(param)).collect(Collectors.joining("\n")
            );

            builder.append(generateTestsForMethodInvocationWithPermutatedArguments(parameters, methodNamePascalCase, methodInvocation, mocks));

            if (generateTestsForIfStatements) {
                builder.append(generataTestsForIfStatements(parsed, methodName, methodNamePascalCase, methodInvocation, arguments, mocks));
                builder.append(generataTestsForTernaryStatements(parsed, methodName, methodNamePascalCase, methodInvocation, arguments, mocks));
                builder.append(geterateTestForSwitchCases(parsed, methodName, methodNamePascalCase, methodInvocation, arguments, mocks));
            }
        });
        builder.append("}\n");
        return builder.toString();
    }

    private String generateMockedVariable(Parameter param) {
        Type type = getBoxedType(param);
        if (((ClassOrInterfaceType) type).isBoxedType()) {
            return  param + " = 42;";
        } else if (((ClassOrInterfaceType) type).getNameAsString().equals("String")) {
            return param + " = \"42\";";
        }
        return param + " = mock(" + type.asString() + ".class);";
    }

    private Type getBoxedType(Parameter param) {
        var type = param.getType();
        if (type.isPrimitiveType()) {
            type = ((PrimitiveType) type).toBoxedType();
        }
        return type;
    }

    private String getMethodInvocation(String methodName, NodeList<Parameter> parameters) {
        return "        var actual = target." + methodName + "(" + parameters.stream()
                .map(Parameter::getNameAsString)
                .collect(Collectors.joining(", "))
                + ");";
    }

    private StringBuilder generateTestsForMethodInvocationWithPermutatedArguments(NodeList<Parameter> parameters, String methodNamePascalCase, String methodInvocation, List<String> mocks) {
        var builder = new StringBuilder();
        generatePermutations(parameters).forEach(p -> {
            builder.append("    @Test\n");
            builder.append(
                    "    void test" + methodNamePascalCase + "Where" +
                            Arrays.stream(p)
                                    .map(param -> {
                                        var split = param.split("\s+");
                                        var variableNamePascalCase = split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                                        return variableNamePascalCase + split[2] + split[3];
                                    })
                                    .map(param -> param.replaceAll("\s*=\s*", "Is"))
                                    .map(param -> param.replaceAll(";", ""))
                                    .map(param -> param.replaceAll("\"", ""))
                                    .map(param -> param.replaceAll("Isnull", "IsNull"))
                                    .map(param -> param.replaceAll("mock\\(((?!\\)).)+\\)", "IsMocked"))
                                    .collect(Collectors.joining("And")) + "() {\n"
            );
            String arguments = Arrays.stream(p).map(param -> "        " + param).collect(Collectors.joining("\n"));
            builder.append("        // GIVEN\n");
            builder.append(arguments);
            builder.append("\n");
            builder.append("\n");
            builder.append("        // WHEN\n");
            builder.append(methodInvocation);
            builder.append("\n");
            builder.append("\n");
            builder.append("        // THEN\n");
            builder.append("        assertThat(actual).isNotNull();\n");
            if (!mocks.isEmpty()) {
                builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
            }
            builder.append("    }\n");
            builder.append("\n");
        });
        return builder;
    }

    private StringBuilder generataTestsForIfStatements(CompilationUnit parsed, String methodName, String methodNamePascalCase, String methodInvocation, String arguments, List<String> mocks) {
        var builder = new StringBuilder();
        parsed.findAll(IfStmt.class).stream()
                .filter(ifStmt -> methodName.equals(getMethodName(ifStmt)))
                .forEach(expression -> {
                    builder.append("    @Test\n");
                    builder.append("    // " + expression.getCondition() + "\n");
                    builder.append("    void testIfConditionIn" + methodNamePascalCase + "() {\n");
                    builder.append("        // GIVEN\n");
                    builder.append(arguments);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // WHEN\n");
                    builder.append(methodInvocation);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // THEN\n");
                    builder.append("        assertThat(actual).isNotNull();\n");
                    if (!mocks.isEmpty()) {
                        builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
                    }
                    builder.append("    }\n");
                    builder.append("\n");
                    if (expression.hasElseBlock()) {
                        builder.append("    @Test\n");
                        builder.append("    // " + expression.getCondition()+ "\n");
                        builder.append("    void testElseConditionIn" + methodNamePascalCase + "() {\n");
                        builder.append("        // GIVEN\n");
                        builder.append(arguments);
                        builder.append("\n");
                        builder.append("\n");
                        builder.append("        // WHEN\n");
                        builder.append(methodInvocation);
                        builder.append("\n");
                        builder.append("\n");
                        builder.append("        // THEN\n");
                        builder.append("        assertThat(actual).isNotNull();\n");
                        if (!mocks.isEmpty()) {
                            builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
                        }
                        builder.append("    }\n");
                        builder.append("\n");
                    }
                });
        return builder;
    }

    private StringBuilder generataTestsForTernaryStatements(CompilationUnit parsed, String methodName, String methodNamePascalCase, String methodInvocation, String arguments, List<String> mocks) {
        var builder = new StringBuilder();
        parsed.findAll(ConditionalExpr.class).stream()
                .filter(ifStmt -> methodName.equals(getMethodName(ifStmt)))
                .forEach(expression -> {
                    builder.append("    @Test\n");
                    builder.append("    // " + expression.getCondition() + "\n");
                    builder.append("    void testTernaryConditionIn" + methodNamePascalCase + "() {\n");
                    builder.append("        // GIVEN\n");
                    builder.append(arguments);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // WHEN\n");
                    builder.append(methodInvocation);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // THEN\n");
                    builder.append("        assertThat(actual).isNotNull();\n");
                    if (!mocks.isEmpty()) {
                        builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
                    }
                    builder.append("    }\n");
                    builder.append("\n");
                    builder.append("    @Test\n");
                    builder.append("    // " + expression.getCondition() + "\n");
                    builder.append("    void testTernaryElseConditionIn" + methodNamePascalCase + "() {\n");
                    builder.append("        // GIVEN\n");
                    builder.append(arguments);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // WHEN\n");
                    builder.append(methodInvocation);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // THEN\n");
                    builder.append("        assertThat(actual).isNotNull();\n");
                    if (!mocks.isEmpty()) {
                        builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
                    }
                    builder.append("    }\n");
                    builder.append("\n");
                });
        return builder;
    }

    private StringBuilder geterateTestForSwitchCases(CompilationUnit parsed, String methodName, String methodNamePascalCase, String methodInvocation, String arguments, List<String> mocks) {
        var builder = new StringBuilder();
        parsed.findAll(SwitchStmt.class).stream()
                .filter(ifStmt -> methodName.equals(getMethodName(ifStmt)))
                .map(SwitchStmt::getEntries)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        switchEntry -> switchEntry.getLabels().stream().map(Objects::toString).collect(Collectors.joining(",")),
                        Collectors.mapping(switchEntry -> switchEntry.getStatements().stream().map(Objects::toString).collect(Collectors.joining()), Collectors.toList())
                )).forEach((caze, statement) -> {
                    builder.append("    @Test\n");
                    builder.append("    // case " + caze + ": " + statement.stream().collect(Collectors.joining(" ")) + "\n");
                    builder.append("    void testSwitchCaseConditionIn" + methodNamePascalCase + "() {\n");
                    builder.append("        // GIVEN\n");
                    builder.append(arguments);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // WHEN\n");
                    builder.append(methodInvocation);
                    builder.append("\n");
                    builder.append("\n");
                    builder.append("        // THEN\n");
                    builder.append("        assertThat(actual).isNotNull();\n");
                    if (!mocks.isEmpty()) {
                        builder.append("verifyNoMoreInteractions(" + mocks.stream().collect(Collectors.joining(",")) + ");\n");
                    }
                    builder.append("    }\n");
                    builder.append("\n");
                });
        return builder;
    }

    public String getMethodName(Node node) {
        Optional<Node> currentNode = node.getParentNode();
        while (currentNode.isPresent()) {
            if (currentNode.get() instanceof MethodDeclaration declaration) {
                return declaration.getNameAsString();
            }
            currentNode = currentNode.get().getParentNode();
        }
        return "UNKNOWN_METHOD";
    }

    private List<String[]> generatePermutations(NodeList<Parameter> params) {
        var result = new ArrayList<String[]>();
        int n = params.size();
        int permutations = 1 << n;
        for (int i = 0; i < permutations; i++) {
            String[] permutation = new String[n];
            for (int j = 0; j < n; j++) {
                int bit = (i >> j) & 1;
                permutation[j] = (bit == 0 ? getBoxedType(params.get(j)) + " " +  params.get(j).getNameAsString() + " = null;" : generateMockedVariable(params.get(j)));
            }
            result.add(permutation);
        }
        return result;
    }
}
