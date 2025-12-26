package org.linuxswords.games.tiltmate.time;

import java.util.Objects;

public final class TimeSettings
{
    // Predefined preset constants (singleton-like instances)
    public static final TimeSettings TEST = new TimeSettings(2, 0, "2+0", "TEST");
    public static final TimeSettings THREE_PLUS_ZERO = new TimeSettings(3, 0, "3+0", "THREE_PLUS_ZERO");
    public static final TimeSettings THREE_PLUS_FIVE = new TimeSettings(3, 5, "3+5", "THREE_PLUS_FIVE");
    public static final TimeSettings FIVE_PLUS_ZERO = new TimeSettings(5, 0, "5+0", "FIVE_PLUS_ZERO");
    public static final TimeSettings FIVE_PLUS_FIVE = new TimeSettings(5, 5, "5+5", "FIVE_PLUS_FIVE");
    public static final TimeSettings TEN_PLUS_ZERO = new TimeSettings(10, 0, "10+0", "TEN_PLUS_ZERO");
    public static final TimeSettings TEN_PLUS_FIVE = new TimeSettings(10, 5, "10+5", "TEN_PLUS_FIVE");
    public static final TimeSettings FIFTEEN_PLUS_ZERO = new TimeSettings(15, 0, "15+0", "FIFTEEN_PLUS_ZERO");

    // Special marker for custom times
    public static final String CUSTOM_NAME = "CUSTOM";

    private final int minutes;
    private final int increment;
    private final String label;
    private final String name;

    // Private constructor for preset instances
    private TimeSettings(int minutes, int increment, String label, String name)
    {
        this.minutes = minutes;
        this.increment = increment;
        this.label = label;
        this.name = name;
    }

    /**
     * Create a custom time setting with validation
     * @param minutes The number of minutes (1-180)
     * @param increment The increment in seconds (0-60)
     * @return A new custom TimeSettings instance
     * @throws IllegalArgumentException if values are out of range
     */
    public static TimeSettings createCustom(int minutes, int increment)
    {
        if (minutes < 1 || minutes > 180) {
            throw new IllegalArgumentException("Minutes must be between 1 and 180");
        }
        if (increment < 0 || increment > 60) {
            throw new IllegalArgumentException("Increment must be between 0 and 60");
        }
        String label = minutes + "+" + increment;
        return new TimeSettings(minutes, increment, label, CUSTOM_NAME);
    }

    /**
     * Get all preset time settings (replaces enum.values())
     * @return Array of all preset TimeSettings
     */
    public static TimeSettings[] getPresets()
    {
        return new TimeSettings[] {
            TEST,
            THREE_PLUS_ZERO,
            THREE_PLUS_FIVE,
            FIVE_PLUS_ZERO,
            FIVE_PLUS_FIVE,
            TEN_PLUS_ZERO,
            TEN_PLUS_FIVE,
            FIFTEEN_PLUS_ZERO
        };
    }

    public String getName()
    {
        return name;
    }

    public int getMinutes()
    {
        return minutes;
    }

    public int getIncrement()
    {
        return increment;
    }

    public long minutesAsMilliSeconds()
    {
        return this.minutes * 60L * 1_000L;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * Check if this is a custom time setting
     * @return true if custom, false if preset
     */
    public boolean isCustom()
    {
        return CUSTOM_NAME.equals(name);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TimeSettings)) return false;
        TimeSettings that = (TimeSettings) o;
        return minutes == that.minutes && increment == that.increment;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(minutes, increment);
    }
}
