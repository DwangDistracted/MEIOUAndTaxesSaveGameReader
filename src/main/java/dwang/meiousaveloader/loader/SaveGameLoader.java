package dwang.meiousaveloader.loader;

import com.google.common.base.Strings;
import dwang.meiousaveloader.constants.ProgramConstants;
import dwang.meiousaveloader.model.Country;
import dwang.meiousaveloader.model.CountryTag;
import dwang.meiousaveloader.model.Province;
import dwang.meiousaveloader.model.Save;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaveGameLoader implements Runnable {
    private static Logger logger = LogManager.getLogger(SaveGameLoader.class);

    /**
     * Stores the tags and names of each localized nation tag
     */
    private static Map<CountryTag, String> localizedNationTagsToNames = new HashMap<>();
    /**
     * Stores the tags and names of custom nations and colonies that do not have localization
     */
    private Map<CountryTag, String> customNationTagsToNames = new HashMap<>();

    private File saveFile;
    private boolean includeSubjects;
    private LoadStatus loadStatus;

    private List<String> lines;

    public SaveGameLoader(File saveGame, boolean includeSubjects) throws IOException {
        this.saveFile = saveGame;
        lines = Files.readAllLines(saveFile.toPath(), ProgramConstants.CHARSET);
        this.includeSubjects = includeSubjects;
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

    @Override
    public void run() {
        try {
            this.loadStatus = LoadStatus.LOADING;
            doLoad();
            this.loadStatus = LoadStatus.SUCCESS;
        } catch (LoadInterruptedException e) {
            logger.warn("Load of save file " + saveFile.getName() + " was aborted by user.");
        } catch (SaveLoadException e) {
            logger.error("Could not load save file. Check if save file is corrupted.");
            logger.error(e);
            this.loadStatus = LoadStatus.FAILED;
        }
    }

    public void setStatus(LoadStatus status) {
        this.loadStatus = status;
    }

    public LoadStatus getStatus() {
        return loadStatus;
    }

    private Save doLoad() throws LoadInterruptedException, SaveLoadException {
        logger.info("Starting Load of Save " + saveFile.getName());
        Save save = new Save(saveFile.getName());

        int countryListStart = lines.indexOf("countries={");

        for (int i = countryListStart; i < lines.size(); i++) {
            if (lines.get(i).contains("has_set_government_name=yes")) {
                Country country = loadCountry(i);

                if (null != country) {
                    save.addCountry(country);
                }
            }

            if (isAborted()) {
                throw new LoadInterruptedException();
            }
        }

        logger.info("Successfully Loaded Save " + saveFile.getName());
        return save;
    }

    private Country loadCountry(int referenceIndex) throws SaveLoadException, LoadInterruptedException {
        String countryTag = findTag(referenceIndex);
        if (Strings.isNullOrEmpty(countryTag)) {
            throw new SaveLoadException(saveFile.getName(), SaveLoadException.SaveLoadExceptionType.TAG_NOT_FOUND);
        } else if (ProgramConstants.omittedCountryTags.contains(countryTag)) {
            logger.trace("Skipping Country Tag " + countryTag);
            return null;
        }
        logger.debug("Loading Country " + countryTag);
        Country country = new Country(new CountryTag(countryTag));

        List<String> ownedProvinceIds = findOwnedProvinceIds(countryTag);

        for (String provinceId : ownedProvinceIds) {
            country.addProvince(loadProvince(provinceId));

            if (isAborted()) {
                throw new LoadInterruptedException();
            }
        }

        logger.debug("Loaded Country " + countryTag);
        return country;
    }

    private Province loadProvince(String provinceId) {
        logger.debug("Loading Province " + provinceId);
        lines.indexOf(provinceId);
        return null;
    }

    private String findTag(int referenceIndex) {
        for (int x = referenceIndex; x >= 0; x--) {
            if (lines.get(x).matches("\t[A-Z0-9]{3,4}=[{]")) {
                String tagLine = lines.get(x);
                return tagLine.trim().split("=")[0];
            }
        }

        return null;
    }

    private List<String> findOwnedProvinceIds(String tag) {
        int startIndex = lines.indexOf("\t" + tag + "={");

        for (int i = startIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("owned_provinces={")) {
                return List.of(lines.get(i+1).trim().split(" "));
            } else if (line.matches("\t[A-Z0-9]{3,4}=[{]")) {
                logger.trace("No Owned Provinces");
                break;
            }
        }

        return Collections.emptyList();
    }

    private boolean isAborted() {
        return loadStatus == LoadStatus.ABORTED;
    }

    public enum LoadStatus {
        LOADING("LOADING"),
        SUCCESS("SUCCESS"),
        ABORTED("ABORTED"),
        FAILED("FAILED");

        private final String status;
        LoadStatus(String status) {
            this.status = status;
        }
    }

    private class LoadInterruptedException extends Exception {
    }
}
