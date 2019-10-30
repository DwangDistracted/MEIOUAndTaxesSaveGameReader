package dwang.meiousaveloader.model;

public enum SubjectType {
    VASSAL("Vassal"),
    MARCH("March"),
    TRIBUTARY("Tributary"),
    COLONY("Colony"),
    PERSONAL_UNION("Personal Union");

    private final String type;
    SubjectType(String type) {
        this.type = type;
    }

    public String getName() {
        return type;
    }
}
