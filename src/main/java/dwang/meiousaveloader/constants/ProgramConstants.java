package dwang.meiousaveloader.constants;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

public class ProgramConstants {
    public static final String major = "3";
    public static final String minor = "0";
    public static final String patch = "0";

    public static final String FULL_VERSION = major + "." + minor + "." + patch;

    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    public static final String DEFAULT_LOCALIZATION = "countryList.yml";
    public static final String EU4_EXT = ".eu4";

    public static final Set<String> omittedCountryTags = ImmutableSet.of("---", "REB", "PIR", "NAT", "AAA");

    public static Optional<File> loadResourceFile(String fileName) {
        URL url = ProgramConstants.class.getClassLoader().getResource(fileName);
        return url == null ? Optional.empty() : Optional.of(new File(url.getFile()));
    }
}
