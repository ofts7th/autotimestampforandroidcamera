package cs.timestamp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class SplashActivity extends Activity {
    private String[] permissions = new String[]{Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.splash);
        boolean permissionValid = true;
        for (String p : permissions) {
            permissionValid = (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED);
            if (!permissionValid)
                break;
        }
        if (!permissionValid) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            startApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionValid = true;
        for (int p : grantResults) {
            permissionValid = (p == PackageManager.PERMISSION_GRANTED);
            if (!permissionValid)
                break;
        }
        if (permissionValid) {
            startApp();
        }
    }

    final Activity activity = this;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent i = new Intent(activity, MainActivity.class);
            activity.startActivity(i);
            activity.finish();
        }
    };

    private void startApp() {
        handler.sendEmptyMessage(1);
    }
}