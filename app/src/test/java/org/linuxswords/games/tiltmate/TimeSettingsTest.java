package org.linuxswords.games.tiltmate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.linuxswords.games.tiltmate.time.TimeSettings;


class TimeSettingsTest
{
    // Test-only constant for testing purposes
    private static final TimeSettings TEST = TimeSettings.createCustom(2, 0);

    static Stream<Arguments> timeSettingsProvider()
    {
        return Stream.of(
            arguments(TEST, 2, 0, "2+0", 2L * 60L * 1_000L),
            // Blitz (FIDE official)
            arguments(TimeSettings.THREE_PLUS_ZERO, 3, 0, "3+0", 3L * 60L * 1_000L),
            arguments(TimeSettings.THREE_PLUS_TWO, 3, 2, "3+2", 3L * 60L * 1_000L),
            arguments(TimeSettings.FIVE_PLUS_ZERO, 5, 0, "5+0", 5L * 60L * 1_000L),
            arguments(TimeSettings.FIVE_PLUS_THREE, 5, 3, "5+3", 5L * 60L * 1_000L),
            // Rapid (FIDE official)
            arguments(TimeSettings.TEN_PLUS_ZERO, 10, 0, "10+0", 10L * 60L * 1_000L),
            arguments(TimeSettings.TEN_PLUS_FIVE, 10, 5, "10+5", 10L * 60L * 1_000L),
            arguments(TimeSettings.FIFTEEN_PLUS_TEN, 15, 10, "15+10", 15L * 60L * 1_000L)
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
        TimeSettings[] allSettings = TimeSettings.getPresets();
        assertThat(allSettings).hasSize(7);
        assertThat(allSettings).contains(
            TimeSettings.THREE_PLUS_ZERO,
            TimeSettings.THREE_PLUS_TWO,
            TimeSettings.FIVE_PLUS_ZERO,
            TimeSettings.FIVE_PLUS_THREE,
            TimeSettings.TEN_PLUS_ZERO,
            TimeSettings.TEN_PLUS_FIVE,
            TimeSettings.FIFTEEN_PLUS_TEN
        );
    }

    @Test
    void testCreateCustom()
    {
        TimeSettings custom = TimeSettings.createCustom(20, 10);
        assertThat(custom.getMinutes()).isEqualTo(20);
        assertThat(custom.getIncrement()).isEqualTo(10);
        assertThat(custom.minutesAsMilliSeconds()).isEqualTo(20L * 60L * 1_000L);
    }

    @Test
    void testCustomLabel()
    {
        TimeSettings custom = TimeSettings.createCustom(20, 10);
        assertThat(custom.getLabel()).isEqualTo("20+10");

        TimeSettings custom2 = TimeSettings.createCustom(1, 0);
        assertThat(custom2.getLabel()).isEqualTo("1+0");
    }

    @Test
    void testCustomEquals()
    {
        TimeSettings custom1 = TimeSettings.createCustom(20, 10);
        TimeSettings custom2 = TimeSettings.createCustom(20, 10);
        TimeSettings custom3 = TimeSettings.createCustom(15, 5);

        assertThat(custom1).isEqualTo(custom2);
        assertThat(custom1).isNotEqualTo(custom3);
        assertThat(custom1.hashCode()).isEqualTo(custom2.hashCode());
    }

    @Test
    void testCustomIsCustom()
    {
        TimeSettings custom = TimeSettings.createCustom(20, 10);
        assertThat(custom.isCustom()).isTrue();

        assertThat(TimeSettings.TEN_PLUS_FIVE.isCustom()).isFalse();
        assertThat(TimeSettings.THREE_PLUS_ZERO.isCustom()).isFalse();
    }

    @Test
    void testValidation()
    {
        // Test invalid minutes
        assertThatThrownBy(() -> TimeSettings.createCustom(0, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minutes must be between 1 and 180");

        assertThatThrownBy(() -> TimeSettings.createCustom(181, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minutes must be between 1 and 180");

        // Test invalid increment
        assertThatThrownBy(() -> TimeSettings.createCustom(10, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Increment must be between 0 and 60");

        assertThatThrownBy(() -> TimeSettings.createCustom(10, 61))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Increment must be between 0 and 60");

        // Test valid edge cases
        TimeSettings min = TimeSettings.createCustom(1, 0);
        assertThat(min.getMinutes()).isEqualTo(1);

        TimeSettings max = TimeSettings.createCustom(180, 60);
        assertThat(max.getMinutes()).isEqualTo(180);
        assertThat(max.getIncrement()).isEqualTo(60);
    }
}
