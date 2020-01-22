package dwang.meiousaveloader.loader;

import dwang.meiousaveloader.constants.SaveFileStrings;
import dwang.meiousaveloader.model.Country;
import dwang.meiousaveloader.model.Province;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProvinceLoader extends Thread {
    private static Logger logger = LogManager.getLogger(SaveGameLoader.class);

    private final Country ownerCountry;
    private final int provinceId;
    private final List<String> provinceData;

    ProvinceLoader(Country ownerCountry, int provinceId, List<String> provinceData) {
        this.ownerCountry = ownerCountry;
        this.provinceId = provinceId;
        this.provinceData = new ArrayList<>();
        this.provinceData.addAll(provinceData);
    }

    @Override
    public void run() {
        Optional<Province> province = doLoad();
        province.ifPresent(ownerCountry::addProvince);
    }

    private Optional<Province> doLoad() {
        Province province = new Province(provinceId);

        for (String line : provinceData) {
            if (line.startsWith(SaveFileStrings.provinceNameAttr)) {
                String provinceName = line.split("=")[1].replace("\"", "").trim();
                province.setName(provinceName);
                logger.trace("Province Name found: " + provinceName);
            } else if (line.startsWith(SaveFileStrings.provinceCityNameAttr)) {
                String cityName = line.split("=")[1].replace("\"", "").trim();
                province.setCity(cityName);
                logger.trace("Province City found: " + cityName);
            }
        }

        return Optional.of(province);
    }
}
