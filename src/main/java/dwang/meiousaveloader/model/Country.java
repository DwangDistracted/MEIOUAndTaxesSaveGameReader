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

    public void addProvince(Province province) {
        ownedProvinces.add(province);
    }

    public List<Province> getProvince() {
        return ownedProvinces;
    }

    public void addAlly(CountryTag tag) {
        allies.add(tag);
    }

    public List<CountryTag> getAllies () {
        return allies;
    }

    public void addSubject(CountryTag tag, SubjectType type) {
        subjects.put(tag, type);
    }

    public Map<CountryTag, SubjectType> getSubjects() {
        return subjects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
