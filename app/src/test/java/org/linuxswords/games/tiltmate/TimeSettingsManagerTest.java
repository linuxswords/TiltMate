package org.linuxswords.games.tiltmate;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.linuxswords.games.tiltmate.time.TimeSettings;
import org.linuxswords.games.tiltmate.time.TimeSettingsManager;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
class TimeSettingsManagerTest
{
    private TimeSettingsManager manager;
    private Context context;

    @Before
    public void setUp()
    {
        context = ApplicationProvider.getApplicationContext();
        // Clear SharedPreferences before each test
        context.getSharedPreferences("TiltMatePrefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        manager = TimeSettingsManager.instance(context);
    }

    @Test
    public void testInstanceReturnsSameInstance()
    {
        TimeSettingsManager instance1 = TimeSettingsManager.instance(context);
        TimeSettingsManager instance2 = TimeSettingsManager.instance(context);

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void testDefaultTimeSettingIsTenPlusFive()
    {
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.TEN_PLUS_FIVE);
    }

    @Test
    public void testSetAndGetCurrent()
    {
        manager.setCurrent(TimeSettings.FIVE_PLUS_ZERO);
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.FIVE_PLUS_ZERO);

        manager.setCurrent(TimeSettings.THREE_PLUS_FIVE);
        assertThat(manager.getCurrent()).isEqualTo(TimeSettings.THREE_PLUS_FIVE);
    }

    @Test
    public void testSetCurrentChangesGlobalState()
    {
        manager.setCurrent(TimeSettings.FIFTEEN_PLUS_FIVE);

        // Getting instance again should have the same setting
        TimeSettingsManager anotherReference = TimeSettingsManager.instance(context);
        assertThat(anotherReference.getCurrent()).isEqualTo(TimeSettings.FIFTEEN_PLUS_FIVE);
    }

    @Test
    public void testAllTimeSettingsCanBeSet()
    {
        for (TimeSettings setting : TimeSettings.values()) {
            manager.setCurrent(setting);
            assertThat(manager.getCurrent()).isEqualTo(setting);
        }
    }

    @Test
    public void testSettingPersistsAcrossInstances()
    {
        // Set a value
        manager.setCurrent(TimeSettings.THREE_PLUS_ZERO);

        // Create a new manager instance (simulating app restart)
        TimeSettingsManager newManager = TimeSettingsManager.instance(context);

        // Should load the saved value
        assertThat(newManager.getCurrent()).isEqualTo(TimeSettings.THREE_PLUS_ZERO);
    }
}
