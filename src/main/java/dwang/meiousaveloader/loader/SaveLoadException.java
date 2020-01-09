package dwang.meiousaveloader.loader;

public class SaveLoadException extends Exception {
    public SaveLoadException (String saveName, SaveLoadExceptionType reason) {
        super("Could Not Load Save File " + saveName + " Due to Error: " + reason.toString());
    }

    public enum SaveLoadExceptionType {
        TAG_NOT_FOUND("Could not find country tag"),
        CORRUPT_DATA_FORMAT("Load File Country Data is Corrupted");

        private String message;
        SaveLoadExceptionType(String message) {
            this.message = message;
        }
        @Override
        public String toString() {
            return message;
        }

    }
}
