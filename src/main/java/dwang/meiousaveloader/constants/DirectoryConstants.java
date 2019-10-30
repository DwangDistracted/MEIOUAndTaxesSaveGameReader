package dwang.meiousaveloader.constants;

import java.io.File;
import java.util.Optional;

public class DirectoryConstants {
    private static final String folder_delimiter = "\\";

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String EU4_DOCUMENTS_DIR =
            USER_HOME + folder_delimiter + "Documents\\Paradox Interactive\\Europa Universalis IV";
    public static final String EU4_SAVE_GAME_DIR = EU4_DOCUMENTS_DIR + folder_delimiter + "save games";

    private static File saveGameDirectory;

    public static void setSaveGameDirectory(File dir) {
        saveGameDirectory = dir;
    }

    public static Optional<File> getSaveGameDirectory() {
        return Optional.ofNullable(saveGameDirectory);
    }
}
