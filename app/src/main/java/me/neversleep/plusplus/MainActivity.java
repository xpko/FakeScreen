package me.neversleep.plusplus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.InputDeviceCompat;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences xConf;

    public static int getActiveVersion() {
        return 0;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        main();
    }

    private void checkEdXposed() {
        try {
            this.xConf = getSharedPreferences("x_conf", MODE_WORLD_READABLE);
        } catch (SecurityException unused) {
            new AlertDialog.Builder(this).setMessage(getString(R.string.not_supported)).setPositiveButton(android.R.string.ok, (dialogInterface, i) -> finish()).setNegativeButton(R.string.ignore, null).show();
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 10002 || iArr.length <= 0) {
            return;
        }
        if (iArr[0] == 0) {
            main();
        } else {
            Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
        }
    }

    public void onResume() {
        super.onResume();
        main();
    }

    @SuppressLint("RestrictedApi")
    protected void main() {
        TextView textView = findViewById(R.id.tips);
        TextView textView2 = findViewById(R.id.shell_control);
        SwitchCompat switchCompat = findViewById(R.id.power);
        String command = "am start -n me.neversleep.plusplus/.MainActivity --ez power true/false";
        textView2.setText(String.format(getString(R.string.shell_control), "am start -n me.neversleep.plusplus/.MainActivity --ez power true/false"));
        textView2.setOnLongClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, command));
                Toast.makeText(MainActivity.this, getString(R.string.copy_success), Toast.LENGTH_LONG).show();
                return true;
            }
            Toast.makeText(MainActivity.this, getString(R.string.copy_failed), Toast.LENGTH_LONG).show();
            return false;

        });
        if (getActiveVersion() == 0) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.active_tips);
            textView.setTextColor(SupportMenu.CATEGORY_MASK);
        } else if (6 == getActiveVersion()) {
            textView.setVisibility(View.INVISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(getString(R.string.active_warn), Integer.valueOf(getActiveVersion()), 6));
            textView.setTextColor(InputDeviceCompat.SOURCE_ANY);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                SharedPreferences sharedPreferences = MainActivity.this.xConf;
                if (sharedPreferences != null) {
                    if (!sharedPreferences.edit().putBoolean("power", z).commit()) {
                        Toast.makeText(MainActivity.this, getString(R.string.failed_tips), 1).show();
                    }
                    if (Build.VERSION.SDK_INT < 24) {
                        return;
                    }
                    TileService.requestListeningState(MainActivity.this, new ComponentName(BuildConfig.APPLICATION_ID, QuickStartService.class.getName()));
                }

            }
        });
        checkEdXposed();
        if (this.xConf == null) {
            Toast.makeText(this, "error: xConf is null!", Toast.LENGTH_LONG).show();
        } else if (getIntent().hasExtra("power")) {
            if (!this.xConf.edit().putBoolean("power", getIntent().getBooleanExtra("power", false)).commit()) {
                Toast.makeText(this, "error: change power failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
            }
            if (Build.VERSION.SDK_INT >= 24) {
                TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickStartService.class.getName()));
            }
            finish();
        } else {
            SharedPreferences sharedPreferences = this.xConf;
            if (sharedPreferences == null) {
                return;
            }
            switchCompat.setChecked(sharedPreferences.getBoolean("power", false));
        }
    }


}
