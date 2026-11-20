package com.dance.dance_card_generator.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Saves the leads and follows to a JSON file, and reads them back when the app starts again.
 *
 * The folder comes from "dancecards.data-folder" in application.properties
 * (normally ~/DanceCards), so the file lives outside the project and survives rebuilds.
 *
 * @Component tells Spring to create one DancerListStore and hand it to any class that asks for it.
 */
@Component
public class DancerListStore {

    private static final Logger log = LoggerFactory.getLogger(DancerListStore.class);
    private static final String FILE_NAME = "dancers.json";

    private final Path file;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    /** Writes one name per line with "key": value spacing, so the file is easy to read and edit by hand. */
    private final ObjectWriter prettyWriter = jsonMapper.writer().with(
            new DefaultPrettyPrinter()
                    .withSeparators(Separators.createDefaultInstance()
                            .withObjectNameValueSpacing(Separators.Spacing.AFTER))
                    .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

    /** @Value fills in the folder setting from application.properties. */
    public DancerListStore(@Value("${dancecards.data-folder}") String folder) {
        this.file = Path.of(folder).resolve(FILE_NAME).toAbsolutePath();
    }

    /** The full path of the save file, e.g. /Users/you/DanceCards/dancers.json. */
    public Path getFile() {
        return file;
    }

    /**
     * Reads the saved names. Returns an empty Optional (meaning "nothing saved") if the file
     * doesn't exist yet or can't be read; it never throws, so a broken file can't stop the app.
     */
    public Optional<DancerLists> load() {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            String json = Files.readString(file);
            return Optional.ofNullable(jsonMapper.readValue(json, DancerLists.class));
        } catch (IOException | RuntimeException e) {
            // RuntimeException covers badly formed JSON (Jackson's errors are RuntimeExceptions).
            log.warn("Couldn't read the saved names from {}, so starting with empty boxes.", file, e);
            return Optional.empty();
        }
    }

    /**
     * Saves the names, creating the folder if needed.
     *
     * Writes to a temporary file first, then swaps it into place in one step, so if the app is
     * stopped halfway through, the old file is still whole rather than half-written.
     */
    public void save(DancerLists lists) throws IOException {
        Files.createDirectories(file.getParent());
        Path tempFile = file.resolveSibling(FILE_NAME + ".tmp");
        Files.writeString(tempFile, prettyWriter.writeValueAsString(lists) + System.lineSeparator());
        Files.move(tempFile, file, REPLACE_EXISTING, ATOMIC_MOVE);
    }
}
