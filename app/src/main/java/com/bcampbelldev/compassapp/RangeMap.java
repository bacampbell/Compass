package com.bcampbelldev.compassapp;

import java.util.ArrayList;
import java.util.List;



class RangeMap {
    static class RangeEntry {
        private final int lower;
        private final int upper;
        private final String value;

        RangeEntry( int lower, int upper, String mappedVal) {
            this.lower = lower;
            this.upper = upper;
            this.value = mappedVal;
        }

        boolean matches(int value) {
            return value >= lower && value <= upper;
        }

        String getValue() {
            return value;
        }
    }


    private final List<RangeEntry> entries = new ArrayList<RangeEntry>();

    void put( int lower, int upper, String mappedVal) {
        entries.add(new RangeEntry(lower, upper, mappedVal));
    }

    String getValueForKey (int key) {
        for (RangeEntry entry : entries) {
            if (entry.matches(key)) return entry.getValue();
        }
        return null;
    }
}
