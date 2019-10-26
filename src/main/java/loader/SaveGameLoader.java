package loader;

import constants.ProgramConstants;
import model.CountryTag;
import model.Save;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaveGameLoader {
    private static Logger logger = LogManager.getLogger(SaveGameLoader.class);

    /**
     * Stores the tags and names of each localized nation tag
     */
    private static Map<CountryTag, String> localizedNationTagsToNames = new HashMap<>();
    /**
     * Stores the tags and names of custom nations and colonies that do not have localization
     */
    private Map<CountryTag, String> customNationTagsToNames = new HashMap<>();

    private File saveGame;

    SaveGameLoader(File saveGame) {
        this.saveGame = saveGame;
    }

    public Save doLoad() {
        return null;
    }

    public static void init() {
        Optional<File> localizationFile = ProgramConstants.loadResourceFile(ProgramConstants.DEFAULT_LOCALIZATION);
        localizationFile.ifPresentOrElse(
                (file) -> {
                    if (!file.canRead()) {
                        logger.warn("Cannot Read Localization File");
                        return;
                    }

                    try {
                        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);

                        for (String line : lines) {
                            String[] lineAr = line.split(":");

                            if (!line.contains("ADJ") && lineAr.length == 2) {
                                String tag = lineAr[0];
                                String name = lineAr[1];
                                name = name.replace("1", "");
                                name = name.replace("\"", "");

                                localizedNationTagsToNames.put(new CountryTag(tag.trim()), name.trim());
                            }
                        }

                        logger.debug("Country Names Initialized");
                    } catch (IOException e) {
                        logger.warn("Could not read localization file");
                        e.printStackTrace();
                    }
                },
                () -> {
                    logger.warn("No Localization File Found");
                });
    }
}
