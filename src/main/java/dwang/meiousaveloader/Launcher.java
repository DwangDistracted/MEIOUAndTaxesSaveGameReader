package dwang.meiousaveloader;


import dwang.meiousaveloader.constants.DirectoryConstants;
import dwang.meiousaveloader.loader.SaveGameLoader;
import dwang.meiousaveloader.view.browser.SaveGameSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static dwang.meiousaveloader.constants.ProgramConstants.FULL_VERSION;

/**
 * Program Entry Point
 */
public class Launcher {
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

        logger.trace("Found the EU4 Save Game Directory at '" + DirectoryConstants.getSaveGameDirectory().get().getAbsolutePath() + "'");
        SaveGameLoader.init();

        new SaveGameSelector();
    }

    private static boolean findSaveGameDir(String sgDir) {
        File dir = new File(sgDir);
        if (!dir.canRead() || !dir.isDirectory()) {
            // TODO - prompt user for save game directory then retry
            return false;
        }

        DirectoryConstants.setSaveGameDirectory(dir);
        return true;
    }
}
