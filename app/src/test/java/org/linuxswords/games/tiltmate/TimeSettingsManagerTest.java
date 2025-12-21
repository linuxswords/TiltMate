package org.linuxswords.games.tiltmate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linuxswords.games.tiltmate.time.TimeSettings;
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;


class TimeSettingsManagerTest
{
    private TimeSettingsManager manager;

    @BeforeEach
    void setUp()
    {
        manager = TimeSettingsManager.instance();
        // Reset to default for each test
        manager.setCurrent(TimeSettings.TEN_PLUS_FIVE);
    }

    @Test
    void testInstanceReturnsSameInstance()
    {
        TimeSettingsManager instance1 = TimeSettingsManager.instance();
        TimeSettingsManager instance2 = TimeSettingsManager.instance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    void testDefaultTimeSettingIsTenPlusFive()
    {
        // After reset in setUp
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.TEN_PLUS_FIVE);
    }

    @Test
    void testSetAndGetCurrent()
    {
        manager.setCurrent(TimeSettings.FIVE_PLUS_ZERO);
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.FIVE_PLUS_ZERO);

        manager.setCurrent(TimeSettings.THREE_PLUS_FIVE);
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.THREE_PLUS_FIVE);
    }

    @Test
    void testSetCurrentChangesGlobalState()
    {
        manager.setCurrent(TimeSettings.FIFTEEN_PLUS_FIVE);

        // Getting instance again should have the same setting
        TimeSettingsManager anotherReference = TimeSettingsManager.instance();
        assertThat(anotherReference.getCurrent()).isEqualTo(TimeSettings.FIFTEEN_PLUS_FIVE);
    }

    @Test
    void testAllTimeSettingsCanBeSet()
    {
        for (TimeSettings setting : TimeSettings.values()) {
            manager.setCurrent(setting);
            assertThat(manager.getCurrent()).isEqualTo(setting);
        }
    }
}
