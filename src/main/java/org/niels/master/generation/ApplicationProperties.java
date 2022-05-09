package org.niels.master.generation;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ApplicationProperties {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

    private final File file;

    public ApplicationProperties(File file) {
        this.file = file;
    }

    public void addLine(String line) {
        try {
            FileUtils.writeStringToFile(file,
                    System.lineSeparator() + line,
                    Charsets.toCharset("UTF8"), true);
        } catch (Exception ex) {
            logger.error("Error appending to application properties", ex);
        }
    }
}
