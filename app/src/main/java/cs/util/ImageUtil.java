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
import java.io.OutputStream;
import java.util.Date;

import cs.string;

/**
 * Created by sunliang on 2019/4/5.
 */

public class ImageUtil {
    public static byte[] converBitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, Integer.valueOf(Util.getConfig("imgJpegQulity")), baos);
        return baos.toByteArray();
    }

    public static void addTimeStamp(String path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmap = drawTextToRightBottom(bitmap, Util.formatDateHM(new Date()), Integer.valueOf(Util.getConfig("txtWatermarkSize")), Color.parseColor("#" + Util.getConfig("txtWatermarkColor")), Integer.valueOf(Util.getConfig("txtWatermarkRightMargin")), Integer.valueOf(Util.getConfig("txtWatermarkBottomMargin")));

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
            Log.d("test", ex.getMessage());
        }
    }

    private static void copyExif(ExifInterface src, ExifInterface dest, String tag) {
        String data = src.getAttribute(tag);
        if (!string.IsNullOrEmpty(data)) {
            dest.setAttribute(tag, data);
        }
    }

    public static Bitmap drawTextToRightBottom(Bitmap bitmap, String text,
                                               int size, int color, int paddingRight, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(paddingRight),
                bitmap.getHeight() - dp2px(paddingBottom));
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

    public static int dp2px(float dp) {
        final float scale = Util.applicationContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}