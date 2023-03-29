package ch.bytecrowd.lazynerd;

import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TemplateFiller {

    /**
     * @param template
     * @param paramProvider
     * @return
     */
    public String fillUpTemplate(String template, ParamProvider paramProvider) {
        return StringSubstitutor.replace(
                template, paramProvider.provideParams()
        );
    }

    /**
     * @param pathToFile
     * @param template
     * @param paramProvider
     * @throws IOException
     */
    public void fillUpTemplateAndWriteToFile(
            String pathToFile,
            String template,
            ParamProvider paramProvider
    ) throws IOException {
        var path = Path.of(pathToFile);
        path.getParent().toFile().mkdirs();
        Files.writeString(
                path,
                fillUpTemplate(template, paramProvider),
                StandardOpenOption.WRITE
        );
    }
}
