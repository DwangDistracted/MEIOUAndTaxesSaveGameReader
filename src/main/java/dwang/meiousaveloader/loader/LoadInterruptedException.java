package dwang.meiousaveloader.loader;

public class LoadInterruptedException extends Exception {
    public LoadInterruptedException(String saveName) {
        super("User aborted load of save game " + saveName);
    }
}