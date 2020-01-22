package dwang.meiousaveloader.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Country {
    private final String tag;
    private String name;

    private final List<Province> ownedProvinces;
    private final List<String> allies;
    private final Map<String, SubjectType> subjects;

    public Country(String tag) {
        this.tag = Objects.requireNonNull(tag, "Cannot have a null tag");
        if (tag.length() > 3) {
            throw new IllegalArgumentException("A Tag must be Three Characters. Tag Received: " + tag);
        }

        ownedProvinces = new ArrayList<>();
        allies = new ArrayList<>();
        subjects = new HashMap<>();
    }

    public synchronized void addProvince(Province province) {
        ownedProvinces.add(province);
    }
    public synchronized List<Province> getProvinces() {
        return ownedProvinces;
    }

    public void addAlly(String tag) {
        allies.add(tag);
    }
    public List<String> getAllies() {
        return allies;
    }

    public void addSubject(String tag, SubjectType type) {
        subjects.put(tag, type);
    }
    public Map<String, SubjectType> getSubjects() {
        return subjects;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public int getTotalDevelopment() {
        return ownedProvinces.stream().mapToInt(Province::getTotalDevelopment).sum();
    }
    public int getTotalPopulation() {
        return ownedProvinces.stream().mapToInt(Province::getTotalPopulation).sum();
    }

    // TODO - getPopulationClass and getPopulationReligionBreakdown
}
