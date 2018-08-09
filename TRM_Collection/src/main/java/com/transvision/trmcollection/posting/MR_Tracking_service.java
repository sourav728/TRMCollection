package com.transvision.trmcollection.posting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.transvision.trmcollection.values.FunctionCalls;

public class MR_Tracking_service extends Service {
    FunctionCalls functionCalls;

    public MR_Tracking_service() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        functionCalls = new FunctionCalls();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start_mr_track();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start_mr_track() {
        functionCalls.logStatus("MR Tracking Checking...");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MR_Track_Notification.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmRunning) {
            functionCalls.logStatus("MR Tracking Started..");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (60000 * 5), pendingIntent);
            }
        } else functionCalls.logStatus("MR Tracking Already running..");
    }

    private void stop_mr_track() {
        functionCalls.logStatus("MR Tracking Checking...");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MR_Track_Notification.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmRunning) {
            functionCalls.logStatus("MR Tracking Stopping..");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        } else functionCalls.logStatus("MR Tracking Not yet Started..");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop_mr_track();
    }
}
