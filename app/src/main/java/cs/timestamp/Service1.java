package cs.timestamp;

import cs.string;
import cs.util.DbHelper;
import cs.util.Util;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CallLog;

import org.json.JSONObject;

import java.util.Date;

public class Service1 extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    Object objLock = new Object();
    static Service1 instance;
    static final int TimeKick = 1;

    static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TimeKick:
                    instance.work();
                    break;
            }
        }
    };

    //public static int value = 0;
    ImageAutoProcessor processor;

    private void startImageProcessor() {
        if (processor == null) {
            processor = new ImageAutoProcessor();
            processor.start();
        }
    }

    void work() {
        //value++;
        //Util.log(String.valueOf(value));
    }

    Thread mainThread = null;

    void init() {
        if (instance == null) {
            instance = this;
            //instance.stop();
        }
        if (mainThread == null) {
            mainThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startImageProcessor();
//                    while (true) {
//                        try {
//                            handler.sendEmptyMessage(TimeKick);
//                            Thread.sleep(300 * 1000);
//                        } catch (InterruptedException e) {
//                        }
//                    }
                }
            });
            mainThread.start();
        }
        //mainThread.interrupt();
    }

//    void stop(){
//        synchronized (objLock) {
//            if (mainThread != null) {
//                mainThread.interrupt();
//            }
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //work();
        synchronized (objLock) {
            this.init();
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}