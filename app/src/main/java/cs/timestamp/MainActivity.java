package cs.timestamp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cs.mobile.WebViewActivity;
import cs.string;
import cs.util.ImageUtil;
import cs.util.Util;

public class MainActivity extends WebViewActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWebView();

        startBgService();
    }

    public void startBgService() {
        Intent intent = new Intent(this, Service1.class);
        this.startService(intent);
    }
}