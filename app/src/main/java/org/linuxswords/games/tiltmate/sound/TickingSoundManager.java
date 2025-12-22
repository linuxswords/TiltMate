package org.linuxswords.games.tiltmate.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

/**
 * Manages the clock ticking sound effect
 * Plays a continuous looping ticking sound while the clock is running
 * The entire tick.wav file is looped seamlessly
 */
public class TickingSoundManager
{
    private static final String TAG = "TickingSoundManager";

    private final SoundPool soundPool;
    private int tickSoundId = -1;
    private int currentStreamId = -1;
    private boolean isEnabled = false; // Default: disabled
    private boolean isPlaying = false;
    private boolean soundLoaded = false;

    public TickingSoundManager(Context context)
    {
        // Create SoundPool for playing ticking sound
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        // Set up load complete listener
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundLoaded = true;
                Log.d(TAG, "Tick sound loaded and ready (sample ID: " + sampleId + ")");
            } else {
                Log.e(TAG, "Failed to load tick sound, status: " + status);
            }
        });

        // Load tick sound from res/raw/tick.wav
        try {
            int tickResourceId = context.getResources().getIdentifier("tick", "raw", context.getPackageName());
            if (tickResourceId != 0) {
                tickSoundId = soundPool.load(context, tickResourceId, 1);
                Log.d(TAG, "Loading tick sound from resources...");
            } else {
                Log.w(TAG, "Tick sound not found. Add tick.wav to res/raw/ directory");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load tick sound", e);
        }
    }

    /**
     * Enable or disable ticking sound
     */
    public void setEnabled(boolean enabled)
    {
        this.isEnabled = enabled;
        if (!enabled && isPlaying) {
            stop();
        }
    }

    /**
     * Start playing the ticking sound (continuous loop)
     */
    public void start()
    {
        if (!isEnabled || isPlaying || tickSoundId == -1) {
            return;
        }

        if (!soundLoaded) {
            Log.w(TAG, "Cannot start ticking - sound not loaded yet");
            return;
        }

        isPlaying = true;
        playTick();
        Log.d(TAG, "Ticking sound started (looping)");
    }

    /**
     * Stop playing the ticking sound
     */
    public void stop()
    {
        isPlaying = false;
        if (currentStreamId != -1) {
            soundPool.stop(currentStreamId);
            currentStreamId = -1;
            Log.d(TAG, "Ticking sound stopped");
        }
    }

    /**
     * Play the tick sound in continuous loop
     * The sound file will loop seamlessly until stop() is called
     */
    private void playTick()
    {
        if (!isPlaying || !isEnabled) {
            return;
        }

        // Play the tick sound with infinite looping
        // Parameters: play(soundID, leftVolume, rightVolume, priority, loop, rate)
        // - Volume: 0.3f (30%) for subtle background ticking
        // - Priority: 1 (normal)
        // - Loop: -1 = infinite loop (the entire sound file loops continuously)
        // - Rate: 1.0f (normal playback speed)
        currentStreamId = soundPool.play(tickSoundId, 0.3f, 0.3f, 1, -1, 1.0f);

        if (currentStreamId == 0) {
            Log.e(TAG, "Failed to play tick sound (streamId = 0)");
        } else {
            Log.d(TAG, "Playing tick sound with infinite loop (streamId: " + currentStreamId + ")");
        }
    }

    /**
     * Release resources
     */
    public void release()
    {
        stop();
        if (soundPool != null) {
            soundPool.release();
        }
    }
}
