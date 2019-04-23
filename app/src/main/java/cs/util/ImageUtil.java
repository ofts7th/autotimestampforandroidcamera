package cs.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cs.string;

/**
 * Created by sunliang on 2019/4/5.
 */

public class ImageUtil {
    static String showTimeOnImage;
    static String waterMarkSuffix;
    static int txtWatermarkSize;
    static String txtWatermarkColor;
    static int txtWatermarkRightMargin;
    static int txtWatermarkBottomMargin;
    static int imgJpegQulity;
    static int sleepTime;

    static {
        refreshConfig();
    }

    public static void refreshConfig() {
        showTimeOnImage = Util.getConfig("showTimeOnImage", "true");
        waterMarkSuffix = Util.getConfig("waterMarkSuffix", "");
        txtWatermarkSize = Integer.valueOf(Util.getConfig("txtWatermarkSize", "60"));
        txtWatermarkColor = Util.getConfig("txtWatermarkColor", "ffa100");
        txtWatermarkRightMargin = Integer.valueOf(Util.getConfig("txtWatermarkRightMargin", "60"));
        txtWatermarkBottomMargin = Integer.valueOf(Util.getConfig("txtWatermarkBottomMargin", "60"));
        imgJpegQulity = Integer.valueOf(Util.getConfig("imgJpegQulity", "75"));
        sleepTime = Integer.valueOf(Util.getConfig("sleepTime", "10"));
    }

    public static byte[] converBitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, imgJpegQulity, baos);
        return baos.toByteArray();
    }

    public static void addTimeStamp(String path) {
        addTimeStamp(path, null);
    }

    public static void addTimeStamp(String path, Date date) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
//            float ratio = imageWidth > imageHeight ? (float) imageWidth / imageHeight : (float) imageHeight / imageWidth;
//
//            if (ratio > 2) {
//                return;
//            }

            if (date == null) {
                date = new Date();
            }
            String waterMark = "";
            if (showTimeOnImage.equals("false")) {
                waterMark += Util.formatDate(date);
            } else {
                waterMark += Util.formatDateHM(date);
            }

            if (!string.IsNullOrEmpty(waterMarkSuffix)) {
                waterMark += waterMarkSuffix;
            }

            bitmap = drawTextToRightBottom(bitmap, waterMark, txtWatermarkSize, Color.parseColor("#" + txtWatermarkColor), txtWatermarkRightMargin, txtWatermarkBottomMargin, getDpRatio(imageWidth, imageHeight));

            byte[] destBytes = converBitmap2Bytes(bitmap);

            String newPath = path + ".new";
            FileOutputStream fos = new FileOutputStream(newPath);
            fos.write(destBytes);
            fos.close();

            ExifInterface exifSrc = new ExifInterface(path);
            ExifInterface exifDest = new ExifInterface(newPath);

            copyExif(exifSrc, exifDest, ExifInterface.TAG_ORIENTATION);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_DATETIME);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_DATETIME_DIGITIZED);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_MAKE);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_MODEL);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_LATITUDE);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_LONGITUDE);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_LATITUDE_REF);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_LONGITUDE_REF);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_ALTITUDE);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_ALTITUDE_REF);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_TIMESTAMP);
            copyExif(exifSrc, exifDest, ExifInterface.TAG_GPS_DATESTAMP);

            exifDest.saveAttributes();

            File srcFile = new File(path);
            srcFile.delete();
            new File(newPath).renameTo(srcFile);
        } catch (Exception ex) {
            Log.d("exception", ex.getMessage());
        }
    }

    private static void copyExif(ExifInterface src, ExifInterface dest, String tag) {
        String data = src.getAttribute(tag);
        if (!string.IsNullOrEmpty(data)) {
            dest.setAttribute(tag, data);
        }
    }

    public static Bitmap drawTextToRightBottom(Bitmap bitmap, String text,
                                               int size, int color, int paddingRight, int paddingBottom, float dpRatio) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(size * dpRatio);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(bitmap, text, paint, bounds,
                (int) (bitmap.getWidth() - bounds.width() - paddingRight * dpRatio),
                (int) (bitmap.getHeight() - paddingBottom * dpRatio));
    }

    private static Bitmap drawTextToBitmap(Bitmap bitmap, String text,
                                           Paint paint, Rect bounds, int paddingLeft, int paddingTop) {
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }

    public static float getDpRatio(int imgWidth, int imgHeight) {
        float bigger = imgWidth > imgHeight ? (float) imgWidth : (float) imgHeight;
        return bigger / 2000;
    }

    public static String getShottimeByExif(String filePath) {
        try {
            ExifInterface exifSrc = new ExifInterface(filePath);
            return exifSrc.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (Exception ex) {
            return "";
        }
    }

    private static SimpleDateFormat format1 = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private static Date parseDate1(String s) {
        try {
            return format1.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseShotDate(String s) {
        if (string.IsNullOrEmpty(s))
            return null;

        if (Util.isStringMatch(s, "[0-9]{4}:[0-9]{1,2}:[0-9]{1,2}\\s?[0-9]{0,2}:?[0-9]{0,2}:?[0-9]{0,2}")) {
            return parseDate1(s);
        }
        return null;
    }

    public static void processFiles(final ArrayList<String> files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sleepTime);
                    for (String f : files) {
                        ImageUtil.addTimeStamp(f);
                    }
                } catch (Exception ex) {

                }
            }
        }).start();
    }
}