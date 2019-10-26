package constants;

public class DirectoryConstants {
    private static final String folder_delimiter = "\\";

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String EU4_DOCUMENTS_DIR =
            USER_HOME + folder_delimiter + "Documents\\Paradox Interactive\\Europa Universalis IV";
    public static final String EU4_SAVE_GAME_DIR = EU4_DOCUMENTS_DIR + folder_delimiter + "save games";
}
