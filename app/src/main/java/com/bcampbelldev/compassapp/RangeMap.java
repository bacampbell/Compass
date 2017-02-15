package com.bcampbelldev.compassapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that an instance will hold the ranges of the compass heading and their
 * corresponding compass point.
 */
class RangeMap {
    static class RangeEntry {
        private final int lower;
        private final int upper;
        private final String value;

        // Each RangeEntry object has an upper and lower value and the compass point that
        // corresponds to that range.
        RangeEntry( int lower, int upper, String mappedVal) {
            this.lower = lower;
            this.upper = upper;
            this.value = mappedVal;
        }

        // Checks if the given int is within the range of the RangeEntry instance.
        boolean matches(int value) {
            return value >= lower && value <= upper;
        }

        // Getter method.
        String getValue() {
            return value;
        }
    }

    // Each RangeMap instance has a List of object RangeEntry.
    private final List<RangeEntry> entries = new ArrayList<RangeEntry>();

    // Method to add a RangeEntry to the list.
    void put( int lower, int upper, String mappedVal) {
        entries.add(new RangeEntry(lower, upper, mappedVal));
    }

    // Method to retrieve the String value of the range that the parameter (key) falls in.
    String getValueForKey (int key) {
        for (RangeEntry entry : entries) {
            if (entry.matches(key)) return entry.getValue();
        }
        return null;
    }
}
