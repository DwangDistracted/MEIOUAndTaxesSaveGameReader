package dwang.meiousaveloader.model;

import java.util.ArrayList;
import java.util.List;

public class Save {
    private final String saveName;
    private final List<Country> countries;

    public Save(String saveName) {
        this.saveName = saveName;
        this.countries = new ArrayList<>();
    }

    public void addCountry(Country country) {
        countries.add(country);
    }

    public List<Country> getCountries() {
        return countries;
    }
}
