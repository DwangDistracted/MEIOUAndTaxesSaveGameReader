package constants;

import java.io.File;
import java.net.URL;
import java.util.Optional;

public class ProgramConstants {
    public static final String major = "3";
    public static final String minor = "0";
    public static final String patch = "0";

    public static final String FULL_VERSION = major + "." + minor + "." + patch;

    public static final String DEFAULT_LOCALIZATION = "countryList.yml";

    public static Optional<File> loadResourceFile(String fileName) {
        URL url = ProgramConstants.class.getClassLoader().getResource(fileName);
        return url == null ? Optional.empty() : Optional.of(new File(url.getFile()));
    }
}
