package dwang.meiousaveloader.model;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class Province {
    private final int id;
    private String name;
    private String city;
    private String culture;

    private final Map<Development, Integer> development;
    private final Map<PopulationClass, Integer> populationByClass;
    private final Map<String, Double> populationByReligionPercentage;

    // TODO - Production Data

    public Province(int id) {
        this.id = id;
        development = new HashMap<>();
        development.put(Development.TAX, 0);
        development.put(Development.PRODUCTION, 0);
        development.put(Development.MANPOWER, 0);

        populationByClass = new HashMap<>();
        populationByClass.put(PopulationClass.RURAL, 0);
        populationByClass.put(PopulationClass.URBAN, 0);
        populationByClass.put(PopulationClass.NOBLE, 0);

        populationByReligionPercentage = new HashMap<>();
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getCulture() {
        return culture;
    }
    public void setCulture(String culture) {
        this.culture = culture;
    }

    public void setDevelopment(Development type, int amount) {
        development.replace(type, amount);
    }
    public ImmutableMap<Development, Integer> getDetailedDevelopment() {
        return ImmutableMap.copyOf(development);
    }
    public int getTotalDevelopment() {
        return development.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addPopulation(PopulationClass popClass, int amount) {
        populationByClass.replace(popClass, amount);
    }
    public ImmutableMap<PopulationClass, Integer> getDetailedPopulation() {
        return ImmutableMap.copyOf(populationByClass);
    }
    public int getTotalPopulation() {
        return populationByClass.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addReligiousPopulationPercentage(String religion, double percentage) {
        if (percentage > 1 ||
                populationByReligionPercentage.values().stream().mapToDouble(Double::doubleValue).sum() + percentage > 1) {
            throw new IllegalArgumentException("Religious Percentages are adding up to greater than 1");
        }

        populationByReligionPercentage.put(religion, percentage);
    }
    public ImmutableMap<String, Double> getReligiousPopulationBreakdown() {
        Map<String, Double> breakdown = new HashMap<>();
        int totalPops = getTotalPopulation();

        for (String religion : populationByReligionPercentage.keySet()) {
            breakdown.put(religion, populationByReligionPercentage.get(religion) * totalPops);
        }

        return ImmutableMap.copyOf(breakdown);
    }

    public enum Development {
        TAX,
        PRODUCTION,
        MANPOWER;
    }
    public enum PopulationClass {
        RURAL,
        URBAN,
        NOBLE;
    }
}
