package cs.timestamp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

import cs.string;
import cs.util.Util;

public class Receiver1 extends BroadcastReceiver {
    static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context ctx, Intent i) {
        if (i.getAction().equals(BOOT_ACTION)) {
            Intent intent = new Intent(ctx, Service1.class);
            ctx.startService(intent);
        }
    }
}