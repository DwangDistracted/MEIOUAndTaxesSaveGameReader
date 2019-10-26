package model;

import java.util.Objects;

public class CountryTag {
    private final String tag;

    public CountryTag (String tag) {
        tag = Objects.requireNonNull(tag, "Cannot have a null tag");
        if (tag.length() > 3) {
            throw new IllegalArgumentException("A tag must be Three Characters");
        }
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return true;
        }

        if (!(other instanceof CountryTag)) {
            return false;
        }

        return getTag().equals(((CountryTag) other).getTag());
    }
}
