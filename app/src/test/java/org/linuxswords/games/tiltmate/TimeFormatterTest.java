package org.linuxswords.games.tiltmate;

import static java.util.stream.Stream.of;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.linuxswords.games.tiltmate.time.TimeFormatter;


class TimeFormatterTest
{
    static Stream<Arguments> testTime()
    {
        return of(
            arguments(5L * 60L * 1_000L, "5:00"),
            arguments(10L * 60L * 1_000L, "10:00"),
            arguments(10L * 1_000L, "0:10"),
            arguments(1L * 1_000L, "0:01")
        );
    }

    @ParameterizedTest
    @MethodSource("testTime")
    void testTimeFormatShouldMatchExpectations(long timeInMillis, String expectedResult)
    {
        Assertions.assertThat(TimeFormatter.convertMillisIntoDisplayableTime(timeInMillis)).isEqualTo(expectedResult);
    }


}
