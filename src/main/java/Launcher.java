import constants.DirectoryConstants;
import loader.SaveGameLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static constants.ProgramConstants.FULL_VERSION;

/**
 * Program Entry Point
 */
public class Launcher {
    public static File saveGameDirectory;

    private static final Logger logger = LogManager.getLogger(Launcher.class);

    public static void main(String[] args) {
        logger.info("==================================");
        logger.info("MEIOU AND TAXES - Save Game Reader");
        logger.info("Version " + FULL_VERSION);
        logger.info("/u/XplodingDucks and David Wang");
        logger.info("==================================");

        if (!findSaveGameDir(DirectoryConstants.EU4_SAVE_GAME_DIR)) {
            logger.fatal("Could not find the EU4 Save Game Directory - Exiting...");
            System.exit(1);
        }

        logger.debug("Found the EU4 Save Game Directory at '" + saveGameDirectory.getAbsolutePath() + "'");
        SaveGameLoader.init();
    }

    private static boolean findSaveGameDir(String sgDir) {
        File dir = new File(sgDir);
        if (!dir.exists() || !dir.canRead() || !dir.isDirectory()) {
            // TODO - prompt user for save game directory then retry
            return false;
        }

        saveGameDirectory = dir;
        return true;
    }
}
