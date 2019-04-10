package cs.timestamp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

import cs.string;
import cs.util.ImageUtil;
import cs.util.Util;

/**
 * Created by sunliang on 2019/4/5.
 */

public class ImageAutoProcessor {
    public static boolean monitorWorking;
    static {
        monitorWorking = !Util.getSafeConfig("monitorWorking").equals("false");
    }
    private Context ctx;

    ArrayList<String> dirCameraPathList = new ArrayList<>();

    public ImageAutoProcessor(Context ctx) {
        this.ctx = ctx;
        String dirCameraPath = Util.getConfig("dirCameraPath");
        if (!string.IsNullOrEmpty(dirCameraPath)) {
            for (String s : dirCameraPath.split(",")) {
                dirCameraPathList.add(s);
            }
        }
    }

    boolean observerCreated = false;
    ArrayList<String> processedItems = null;

    private String lastImageDate;

    public void start() {
        if (observerCreated) {
            return;
        }
        Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        while (cursor.moveToNext()) {
            long modifiedTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            lastImageDate = String.valueOf(modifiedTime);
            cursor.close();
            break;
        }
        this.createObservers();
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
            if(!ImageAutoProcessor.monitorWorking)
                return;

            String whereClause = null;
            if (!string.IsNullOrEmpty(lastImageDate)) {
                whereClause = MediaStore.Images.Media.DATE_MODIFIED + " > " + lastImageDate;
            }
            try {
                ContentResolver resolver = ctx.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, whereClause, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
                ArrayList<String> files = new ArrayList<>();
                boolean isFirst = true;
                while (cursor.moveToNext()) {
                    String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String lowerCaseName = fileName.toLowerCase();
                    if (!lowerCaseName.endsWith(".jpg") && !lowerCaseName.endsWith("jpeg")) {
                        continue;
                    }
                    if (processedItems != null) {
                        if (processedItems.contains(fileName)) {
                            continue;
                        }
                    }
                    if (dirCameraPathList.size() > 0) {
                        boolean found = false;
                        for (String s : dirCameraPathList) {
                            if (fileName.startsWith(s)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            continue;
                        }
                    }
                    if (isFirst) {
                        long modifiedTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                        lastImageDate = String.valueOf(modifiedTime);
                        isFirst = false;
                    }
                    files.add(fileName);
                }
                cursor.close();
                processFiles(files);
                processedItems = files;
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