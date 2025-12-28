package org.linuxswords.games.tiltmate;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class InfoActivity extends Activity
{
    private static final String TAG = "InfoActivity";
    private static final String DONATE_URL = "https://buymeacoffee.com/linuxswords";
    private static final String GITHUB_URL = "https://github.com/linuxswords/TiltMate";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupVersionDisplay();
        setupDonateButton();
        setupGitHubButton();

        // Back button returns to settings screen
        findViewById(R.id.infoBackButton).setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked - returning to settings");
            finish();
        });
    }

    private void setupVersionDisplay()
    {
        TextView versionValue = findViewById(R.id.versionValue);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionValue.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get version name", e);
            versionValue.setText("Unknown");
        }
    }

    private void setupDonateButton()
    {
        MaterialButton donateButton = findViewById(R.id.donateButton);
        donateButton.setOnClickListener(v -> {
            Log.d(TAG, "Donate button clicked - opening: " + DONATE_URL);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL));
            startActivity(browserIntent);
        });
    }

    private void setupGitHubButton()
    {
        MaterialButton githubButton = findViewById(R.id.githubButton);
        githubButton.setOnClickListener(v -> {
            Log.d(TAG, "GitHub button clicked - opening: " + GITHUB_URL);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
            startActivity(browserIntent);
        });
    }
}
