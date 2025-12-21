package org.linuxswords.games.tiltmate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.linuxswords.games.tiltmate.time.TimeSettings;


class TimeSettingsTest
{
    static Stream<Arguments> timeSettingsProvider()
    {
        return Stream.of(
            arguments(TimeSettings.TEST, 2, 0, "2+0", 2L * 60L * 1_000L),
            arguments(TimeSettings.THREE_PLUS_ZERO, 3, 0, "3+0", 3L * 60L * 1_000L),
            arguments(TimeSettings.THREE_PLUS_FIVE, 3, 5, "3+5", 3L * 60L * 1_000L),
            arguments(TimeSettings.FIVE_PLUS_ZERO, 5, 0, "5+0", 5L * 60L * 1_000L),
            arguments(TimeSettings.FIVE_PLUS_FIVE, 5, 5, "5+5", 5L * 60L * 1_000L),
            arguments(TimeSettings.TEN_PLUS_ZERO, 10, 0, "10+0", 10L * 60L * 1_000L),
            arguments(TimeSettings.TEN_PLUS_FIVE, 10, 5, "10+5", 10L * 60L * 1_000L),
            arguments(TimeSettings.FIFTEEN_PLUS_ZERO, 15, 0, "15+0", 15L * 60L * 1_000L),
            arguments(TimeSettings.FIFTEEN_PLUS_FIVE, 15, 5, "15+5", 15L * 60L * 1_000L)
        );
    }

    @ParameterizedTest
    @MethodSource("timeSettingsProvider")
    void testTimeSettingsGetIncrement(TimeSettings setting, int expectedMinutes, int expectedIncrement, String expectedLabel, long expectedMillis)
    {
        assertThat(setting.getIncrement()).isEqualTo(expectedIncrement);
    }

    @ParameterizedTest
    @MethodSource("timeSettingsProvider")
    void testTimeSettingsGetLabel(TimeSettings setting, int expectedMinutes, int expectedIncrement, String expectedLabel, long expectedMillis)
    {
        assertThat(setting.getLabel()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @MethodSource("timeSettingsProvider")
    void testTimeSettingsMinutesAsMilliSeconds(TimeSettings setting, int expectedMinutes, int expectedIncrement, String expectedLabel, long expectedMillis)
    {
        assertThat(setting.minutesAsMilliSeconds()).isEqualTo(expectedMillis);
    }

    @Test
    void testAllTimeSettingsArePresent()
    {
        TimeSettings[] allSettings = TimeSettings.values();
        assertThat(allSettings).hasSize(9);
        assertThat(allSettings).contains(
            TimeSettings.TEST,
            TimeSettings.THREE_PLUS_ZERO,
            TimeSettings.THREE_PLUS_FIVE,
            TimeSettings.FIVE_PLUS_ZERO,
            TimeSettings.FIVE_PLUS_FIVE,
            TimeSettings.TEN_PLUS_ZERO,
            TimeSettings.TEN_PLUS_FIVE,
            TimeSettings.FIFTEEN_PLUS_ZERO,
            TimeSettings.FIFTEEN_PLUS_FIVE
        );
    }

    @Test
    void testTimeSettingsEnumValueOf()
    {
        assertThat(TimeSettings.valueOf("TEN_PLUS_FIVE")).isEqualTo(TimeSettings.TEN_PLUS_FIVE);
        assertThat(TimeSettings.valueOf("THREE_PLUS_ZERO")).isEqualTo(TimeSettings.THREE_PLUS_ZERO);
    }
}
