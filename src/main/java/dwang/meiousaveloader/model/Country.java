package dwang.meiousaveloader.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Country {
    private final CountryTag tag;
    private String name;

    private final List<Province> ownedProvinces;
    private final List<CountryTag> allies;
    private final Map<CountryTag, SubjectType> subjects;

    public Country(CountryTag tag) {
        this.tag = Objects.requireNonNull(tag, "Countries cannot have null tags");
        ownedProvinces = new ArrayList<>();
        allies = new ArrayList<>();
        subjects = new HashMap<>();
    }
}
