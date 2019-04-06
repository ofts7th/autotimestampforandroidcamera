package cs.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs.string;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.telephony.TelephonyManager;

@SuppressLint("SimpleDateFormat")
public class Util {
    public static Context applicationContext;

    public static void setApplicationContext(Context cxt) {
        applicationContext = cxt;
    }

    //static
    private static SimpleDateFormat defaultFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat dateHMFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm:ss");
    private static SimpleDateFormat imageDateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    public static String formatDate(Date dt) {
        if (dt == null)
            return null;
        return defaultFormat.format(dt);
    }

    public static String formatDateTime(Date dt) {
        if (dt == null)
            return null;
        return dateTimeFormat.format(dt);
    }

    public static String formatDateHM(Date dt) {
        if (dt == null)
            return null;
        return dateHMFormat.format(dt);
    }

    public static String formatTime(Date dt) {
        if (dt == null)
            return null;
        return timeFormat.format(dt);
    }

    public static Date getDatePart(Date dt) {
        return parseDate(formatDate(dt));
    }

    public static Date getToday() {
        return getDatePart(new Date());
    }

    public static Date parseDateTime(String str) {
        try {
            return dateTimeFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseImageDate(String str) {
        try {
            return imageDateFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getConfig(String k) {
        List<List<String>> result = DbHelper
                .query("Select id, val From config Where name = '" + k + "'");
        if (result.size() == 0)
            return null;
        return result.get(0).get(1);
    }

    public static String getConfig(String k, String defaultVal) {
        String r = getConfig(k);
        if (string.IsNullOrEmpty(r))
            return defaultVal;
        return r;
    }

    public static String getSafeConfig(String k) {
        List<List<String>> result = DbHelper
                .query("Select id, val From config Where name = '" + k + "'");
        if (result.size() == 0)
            return "";
        return result.get(0).get(1);
    }

    public static void saveConfig(String k, String v) {
        ContentValues model = new ContentValues();
        model.put("name", k);
        model.put("val", v);
        List<List<String>> result = DbHelper
                .query("Select id, val From config Where name = '" + k + "'");
        if (result.size() == 0) {
            DbHelper.saveRecrod("config", model);
        } else {
            DbHelper.updateRecrod("config", result.get(0).get(0), model);
        }
    }

    public static String readInStream(InputStream in) {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(in).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public static String getDeviceId() {
        if (string.IsNullOrEmpty(Session.getInstance().getValue("machineId"))) {
            String deviceId = Util.getConfig("deviceId");
            if (string.IsNullOrEmpty(deviceId)) {
                String baseUrl = Util.getConfig("baseUrl");
                if (!string.IsNullOrEmpty(baseUrl)) {
                    String uuid = Util.getWebUrl(baseUrl + "/phone/getuuid.htm");
                    if (!string.IsNullOrEmpty(uuid)) {
                        Session.getInstance().setValue("machineId", uuid);
                        Util.saveConfig("deviceId", uuid);
                    }
                }
            } else {
                Session.getInstance().setValue("machineId", deviceId);
            }
        }
        return Session.getInstance().getValue("machineId");
    }

    private static File rootDir = null;

    public static File getRootDir() {
        if (rootDir == null) {
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                rootDir = Environment.getExternalStorageDirectory();
                rootDir = new File(rootDir.getAbsolutePath(), applicationContext.getPackageName());
            }
            // rootDir = _activity.getApplicationContext().getFilesDir();
        }
        return rootDir;
    }

    public static void compressImage(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1000) {
                Matrix matrix = new Matrix();
                float scaleRatio = (float) 1000 / width;
                matrix.postScale(scaleRatio, scaleRatio);
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                        height, matrix, true);
                file.delete();
                FileOutputStream out;
                out = new FileOutputStream(file);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                out.flush();
                out.close();
            }
        } catch (Exception ex) {

        }
    }

    public static String getWebUrl(String surl) {
        String result = "";
        try {
            URL url = new URL(surl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = Util.readInStream(in);
            in.close();
        } catch (Exception e) {

        }
        return result.trim();
    }

    public static void asyncGetWebUrl(final String surl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getWebUrl(surl);
            }
        }).start();
    }

    public static String postWebData(String surl, String data) {
        String result = "";
        try {
            URL url = new URL(surl);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(data.getBytes());

            InputStream in = new BufferedInputStream(conn
                    .getInputStream());
            result = Util.readInStream(in);
            in.close();
        } catch (Exception e) {

        }
        return result.trim();
    }

    public static void asyncPostWebData(final String url, final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                postWebData(url, data);
            }
        }).start();
    }

    public static String readAssetFile(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String result = "";
        try {
            InputStreamReader reader = new InputStreamReader(applicationContext
                    .getResources().getAssets().open("page" + path));
            BufferedReader r = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            r.close();
            reader.close();
            result = sb.toString();
        } catch (Exception ex) {

        }
        return result.trim();
    }

    public static void log(String m) {
        asyncGetWebUrl("http://192.168.123.230:8080/web/log?m=" + m);
    }

    public static Date parseDate(String str) {
        if (string.IsNullOrEmpty(str))
            return null;
        String eL = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s?[0-9]{0,2}:?[0-9]{0,2}:?[0-9]{0,2}";
        Pattern p = Pattern.compile(eL);
        Matcher m = p.matcher(str);
        if (!m.matches())
            return null;

        String[] arrStr = str.split(" ");
        StringBuilder sb = new StringBuilder();

        String datePart = arrStr[0];
        String[] arrDate = datePart.split("-");
        sb.append(arrDate[0]);
        sb.append("-");
        sb.append(string.padLeft(arrDate[1], 2, "0"));
        sb.append("-");
        sb.append(string.padLeft(arrDate[2], 2, "0"));
        if (arrStr.length == 1) {
            sb.append(" 00:00:00");
        } else {
            sb.append(" ");
            String[] arrTime = arrStr[1].split(":");
            sb.append(string.padLeft(arrTime[0], 2, "0"));
            if (arrTime.length == 1) {
                sb.append(":00:00");
            } else {
                sb.append(":");
                sb.append(string.padLeft(arrTime[1], 2, "0"));
                sb.append(":");
                if (arrTime.length == 2) {
                    sb.append("00");
                } else {
                    sb.append(string.padLeft(arrTime[2], 2, "0"));
                }
            }
        }
        try {
            return dateTimeFormat.parse(sb.toString());
        } catch (Exception ex) {

        }
        return null;
    }

    public static boolean isStringMatch(String str, String p) {
        return Pattern.compile(p).matcher(str).matches();
    }
}