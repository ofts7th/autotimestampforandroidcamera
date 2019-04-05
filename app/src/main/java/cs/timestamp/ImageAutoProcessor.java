package cs.timestamp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import cs.string;
import cs.util.ImageUtil;
import cs.util.Util;

/**
 * Created by sunliang on 2019/4/5.
 */

public class ImageAutoProcessor {
    private Context ctx;

    public ImageAutoProcessor(Context ctx) {
        this.ctx = ctx;
    }

    boolean observerCreated = false;

    public void start() {
        if (observerCreated) {
            return;
        }
        Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        while (cursor.moveToNext()) {
            long modifiedTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            Util.saveConfig("lastImageDate", String.valueOf(modifiedTime));
            cursor.close();
            break;
        }

        setDefaultConfig("txtWatermarkSize", "30");
        setDefaultConfig("txtWatermarkColor", "ff6600");
        setDefaultConfig("txtWatermarkRightMargin", "30");
        setDefaultConfig("txtWatermarkBottomMargin", "30");
        setDefaultConfig("imgJpegQulity", "70");

        this.createObservers();
    }

    private void setDefaultConfig(String k, String v) {
        String str = Util.getConfig(k);
        if (string.IsNullOrEmpty(str)) {
            Util.saveConfig(k, v);
        }
    }

    //observer
    MyImageObserver imagesObserver = null;

    public void createObservers() {
        ContentResolver resolver = ctx.getContentResolver();
        imagesObserver = new MyImageObserver();
        resolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imagesObserver);
        this.observerCreated = true;
    }

    public void destroyObservers() {
        if (this.observerCreated) {
            ContentResolver resolver = ctx.getContentResolver();
            resolver.unregisterContentObserver(imagesObserver);
        }
    }

    public class MyImageObserver extends ContentObserver {
        public MyImageObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            String lastSmsDate = Util.getSafeConfig("lastImageDate");
            String whereClause = MediaStore.Images.Media.DATE_MODIFIED + " > " + lastSmsDate;
            try {
                ContentResolver resolver = ctx.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, whereClause, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
                ArrayList<String> files = new ArrayList<>();
                boolean isFirst = true;
                while (cursor.moveToNext()) {
                    if (isFirst) {
                        long modifiedTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                        Util.saveConfig("lastImageDate", String.valueOf(modifiedTime));
                        isFirst = false;
                    }
                    String arg = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    files.add(arg);
                }
                cursor.close();
                processFiles(files);
            } catch (Exception ex) {
            }
        }
    }

    private void processFiles(ArrayList<String> files) {
        if (files.size() == 0)
            return;

        for (String f : files) {
            ImageUtil.addTimeStamp(f);
        }
    }
}