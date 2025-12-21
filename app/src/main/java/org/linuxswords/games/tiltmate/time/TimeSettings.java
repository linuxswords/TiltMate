package org.linuxswords.games.tiltmate.time;

public enum TimeSettings
{
    TEST(2, 0, "2+0"),
    THREE_PLUS_ZERO(3, 0, "3+0"),
    THREE_PLUS_FIVE(3, 5, "3+5"),
    FIVE_PLUS_ZERO(5, 0, "5+0"),
    FIVE_PLUS_FIVE(5, 5, "5+5"),
    TEN_PLUS_ZERO(10, 0, "10+0"),
    TEN_PLUS_FIVE(10, 5, "10+5"),
    FIFTEEN_PLUS_ZERO(15, 0, "15+0"),
    FIFTEEN_PLUS_FIVE(15, 5, "15+5")
    ;

    private final int minutes;
    private final int increment;
    private final String label;
    TimeSettings(int minutes, int increment, String label)
    {
        this.minutes = minutes;
        this.increment = increment;
        this.label = label;
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
}
