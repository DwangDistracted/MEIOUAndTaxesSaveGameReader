package dwang.meiousaveloader.loader;

import com.google.common.base.Strings;
import dwang.meiousaveloader.constants.ProgramConstants;
import dwang.meiousaveloader.constants.SaveFileStrings;
import dwang.meiousaveloader.model.Country;
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
import java.util.stream.Collectors;

public class SaveGameLoader implements Runnable {
    private static Logger logger = LogManager.getLogger(SaveGameLoader.class);

    /**
     * Stores the tags and names of each localized nation tag
     */
    private static Map<String, String> localizedNationTagsToNames = new HashMap<>();

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
                                    localizedNationTagsToNames.put(tag.trim(), name.trim());
                                } catch (IllegalArgumentException e) {
                                    errorsThrown = true;
                                    logger.warn("Error reading localization file: " + e.getMessage());
                                }
                            }
                        }

                        if (errorsThrown) {
                            logger.warn("Country Names Initialized with Errors");
                        } else {
                            logger.trace("Country Names Successfully Initialized");
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

        int countryListStart = lines.indexOf(SaveFileStrings.countriesSectionStartDelimiter);
        int countryListEnd = lines.indexOf(SaveFileStrings.countriesSectionEndDelimiter);

        for (int i = countryListStart; i < countryListEnd; i++) {
            if (lines.get(i).matches(SaveFileStrings.countryTagRegex)) {
                List<String> countryData = findCountryDataBlock(i);

                loadCountry(countryData).ifPresent(
                    (country) -> {
                        save.addCountry(country);
                    }
                );
            }

            if (isAborted()) {
                throw new LoadInterruptedException();
            }
        }

        logger.info("Successfully Loaded Save " + saveFile.getName());
        return save;
    }

    private Optional<Country> loadCountry(List<String> countryData) throws SaveLoadException, LoadInterruptedException {
        String countryTag = findTag(countryData);
        if (Strings.isNullOrEmpty(countryTag)) {
            throw new SaveLoadException(saveFile.getName(), SaveLoadException.SaveLoadExceptionType.TAG_NOT_FOUND);
        } else if (ProgramConstants.omittedCountryTags.contains(countryTag) || !countryData.contains(SaveFileStrings.countryHasName)) {
            logger.debug("Skipping Country Tag " + countryTag);
            return Optional.empty();
        }
        logger.trace("Loading Country " + countryTag);
        Country country = new Country(countryTag);

        country.setName(localizedNationTagsToNames.containsKey(countryTag) ?
                            localizedNationTagsToNames.get(countryTag) :
                            getCountryCustomName(countryData)
                );

        List<Integer> ownedProvinceIds = findOwnedProvinceIds(countryData);
        if (ownedProvinceIds.isEmpty()) {
            logger.debug("Skipping Country Tag " + countryTag);
            return Optional.empty();
        }

        for (Integer provinceId : ownedProvinceIds) {
            loadProvince(provinceId).ifPresent(
                (province) -> {
                    country.addProvince(province);
                }
            );

            if (isAborted()) {
                throw new LoadInterruptedException();
            }
        }

        if (country.getName().equals(ProgramConstants.MISSING_LOCALIZATION)) {
            logger.warn("Loaded Country Tag " + countryTag + " with missing localization");
        } else {
            logger.trace("Loaded Country " + countryTag);
        }
        return Optional.of(country);
    }

    private Optional<Province> loadProvince(int provinceId) {
        logger.trace("Loading Province " + provinceId);
        List<String> provinceData = findProvinceDataBlock(provinceId);

//        findProvinceName(provinceData);
//        collectProductionData(provinceData);
//        collectPopulationData(provinceData);

        return Optional.empty();
    }

    private List<String> findCountryDataBlock(int startIndex) {
        int endOfCountriesSection = lines.indexOf(SaveFileStrings.countriesSectionEndDelimiter);

        for (int i = startIndex + 1; i < endOfCountriesSection; i++) {
            if (lines.get(i).matches(SaveFileStrings.countryTagRegex)) {
                int endIndex = i - 1;
                return lines.subList(startIndex, endIndex);
            }
        }

        return lines.subList(startIndex, endOfCountriesSection);
    }
    private List<String> findProvinceDataBlock(int provinceId) {
        int startIndex = lines.indexOf(SaveFileStrings.buildProvinceMarkerString(provinceId));
        String nextProvinceMarker = SaveFileStrings.buildProvinceMarkerString(provinceId + 1);
        int endIndex = lines.contains(nextProvinceMarker) ?
                            lines.indexOf(nextProvinceMarker) - 1 :
                            lines.indexOf(SaveFileStrings.countriesSectionStartDelimiter);

        return lines.subList(startIndex, endIndex);
    }

    private String findTag(List<String> countryData) {
        if (countryData.get(0).matches(SaveFileStrings.countryTagRegex)) {
            String tagLine = countryData.get(0);
            return tagLine.trim().split("=")[0];
        }

        return null;
    }
    private String getCountryCustomName(List<String> countryData) {
        Optional<String> nameString = countryData.stream().filter((str) -> str.startsWith(SaveFileStrings.countrySetName)).findFirst();
        if (nameString.isPresent()) {
            return nameString.get().split("=")[1].replace("\"", "").trim();
        } else {
            return ProgramConstants.MISSING_LOCALIZATION;
        }
    }
    private List<Integer> findOwnedProvinceIds(List<String> countryData) {
        int ownedProvinceLineIndex = countryData.indexOf(SaveFileStrings.countryOwnedProvinces);
        if (ownedProvinceLineIndex == -1) {
            return Collections.emptyList();
        } else {
            List<String> provinceIds = List.of(countryData.get(ownedProvinceLineIndex+1).trim().split(" "));
            return provinceIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        }
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
