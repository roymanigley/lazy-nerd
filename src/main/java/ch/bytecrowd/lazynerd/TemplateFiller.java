package ch.bytecrowd.lazynerd;

import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;

public class TemplateFiller {

    /**
     * @param template
     * @param paramProvider
     * @return
     */
    public String fillUpTemplate(String template, ParamProvider paramProvider) {
        var templateWithFilledLoops = LoopFiller.fillUpLoops(template, paramProvider);
        return StringSubstitutor.replace(
                templateWithFilledLoops, paramProvider.provideParams()
        );
    }

    /**
     * @param sourceBasePath 'src/main/java' or 'src/test/java'
     * @param template
     * @param paramProvider
     * @throws IOException
     */
    public void fillUpTemplateAndWriteToFile(
            String sourceBasePath,
            String template,
            ParamProvider paramProvider
    ) throws IOException {
        String generated = fillUpTemplate(template, paramProvider);

        String sourceFileName = generated.split("(class|interface) ")[1].split("\s+")[0];


        String sourceFilePath = generated.lines()
                .findFirst()
                .map(firstLine -> firstLine.replaceAll("package (.+);", "$1"))
                .map(packageName -> packageName.replaceAll("\\.", "/"))
                .map(packagePath -> packagePath + "/" + sourceFileName + ".java")
                .orElse("");

        var path = Path.of(sourceBasePath + "/" + sourceFilePath);
        path.getParent().toFile().mkdirs();
        Files.writeString(
                path,
                generated,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE
        );
    }
}

class LoopFiller {
    static String fillUpLoops(String template, ParamProvider paramProvider) {
        while (template.indexOf("#forEach") > -1) {
            var startIndex = template.indexOf("#forEach");
            var endIndex = template.indexOf("#end");
            var forLoop = template.substring(startIndex, endIndex + 4);

            if (forLoop.lastIndexOf("#forEach") != 0) {
                throw new IllegalArgumentException("nested loops are not supported: \n" + forLoop);
            }

            var key = extractKeyInLoop(forLoop);
            if (paramProvider.provideParams().get(key) instanceof Collection<?> collection) {
                if (collection.size() > 0) {
                    var stripedLoopTemplate = stripLoopAndEnd(forLoop, key);
                    var clazz = collection.iterator().next().getClass();
                    var builder = fillLoopTemplate(collection, stripedLoopTemplate, clazz);
                    template = mergeToMainTemplate(template, builder);
                } else {
                    template = template.replace(forLoop, "");
                }
            } else {
                if (paramProvider.provideParams().get(key) == null) {
                    throw new IllegalArgumentException("#forEach(" + key + ") is null:\n" + forLoop);
                }
                throw new IllegalArgumentException("#forEach(" + key + ") has to be an instance of collection:\n" + forLoop);
            }
        }
        return template;
    }

    static private String extractKeyInLoop(String forLoop) {
        return forLoop.replaceAll("\\s", " ").replaceFirst(".*#forEach\\((((?!\\)).)+)\\).*", "$1");
    }

    static private String stripLoopAndEnd(String forLoop, String key) {
        return forLoop.replaceFirst("#forEach\\(" + key + "\\)\s*\n?", "")
                .replaceFirst("#end\s*\n?", "");
    }

    static private StringBuilder fillLoopTemplate(Collection<?> collection, String loopTemplate, Class<?> clazz) {
        var fieldsMap = ReflectionHelper.getFieldsMap(clazz);
        var builder = new StringBuilder();
        collection.forEach(o -> {
            var valueMap = new HashMap<String, Object>();
            fieldsMap.forEach((fieldName, valueExtractor) -> {
                valueMap.put(fieldName, valueExtractor.apply(o));
            });
            builder.append(
                    StringSubstitutor.replace(loopTemplate, valueMap)
            );
        });
        return builder;
    }

    static private String mergeToMainTemplate(String template, StringBuilder builder) {
        return template.replaceFirst("#forEach(((?!#end).|\n))*#end\s*\n?", builder.toString());
    }
}