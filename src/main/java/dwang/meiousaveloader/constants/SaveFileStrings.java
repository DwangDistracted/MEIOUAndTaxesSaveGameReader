package dwang.meiousaveloader.constants;

public class SaveFileStrings {
    public static final String countriesSectionStartDelimiter = "countries={";
    public static final String countriesSectionEndDelimiter = "active_advisors={";

    public static final String countryTagRegex = "\t[A-Z0-9]{3,4}=[{]";
    public static final String countryOwnedProvinces = "owned_provinces={";
    public static final String countryHasName = "\t\thas_set_government_name=yes";
    public static final String countrySetName = "\t\tname=\"";

    public static final String provinceNameAttr = "name=";
    public static final String provinceCityNameAttr = "capital=";
    public static final String provinceSizeAttr = "prov_size=";

    public static final String provincePopAttr = "total_pop_display";
    public static final String provinceUrbanPopAttr = "urban_pop_display";

    public static final String provinceCultureAttr = "culture=";
    public static final String provinceOriginalCultureAttr = "original_culture=";
    public static final String provinceTribalCultureAttr = "tribal_culture=";

    public static final String provinceReligionAttr = "religion=";
    public static final String provinceOriginalReligionAttr = "original_religion=";

    public static String buildTagMarkerString (String tag) {
        return "\t" + tag + "={";
    }

    public static String buildProvinceMarkerString(Integer provinceId) {
        return "-" + provinceId + "={";
    }

    public static String buildReligiousMinorityMarkerString(String religion) {
        return "";
    }
}
