package dwang.meiousaveloader.loader;

import dwang.meiousaveloader.constants.ProgramConstants;
import jdk.jshell.spi.ExecutionControl;
import dwang.meiousaveloader.model.CountryTag;
import dwang.meiousaveloader.model.Save;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
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

    public SaveGameLoader(File saveGame) {
        this.saveGame = saveGame;
    }

    public Save doLoad() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("SaveGameLoader::doLoad()");
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
                        List<String> lines = Files.readAllLines(file.toPath(), ProgramConstants.CHARSET);
                        boolean errorsThrown = false;

                        for (String line : lines) {
                            String[] lineAr = line.split(":");

                            if (!line.contains("ADJ") && lineAr.length == 2) {
                                String tag = lineAr[0];
                                String name = lineAr[1];
                                name = name.replace("1", "");
                                name = name.replace("\"", "");

                                try {
                                    localizedNationTagsToNames.put(new CountryTag(tag.trim()), name.trim());
                                } catch (IllegalArgumentException e) {
                                    errorsThrown = true;
                                    logger.warn("Error reading localization file: " + e.getMessage());
                                }
                            }
                        }

                        if (errorsThrown) {
                            logger.warn("Country Names Initialized with Errors");
                        } else {
                            logger.debug("Country Names Successfully Initialized");
                        }
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
