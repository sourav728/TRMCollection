package com.transvision.trmcollection.posting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.transvision.trmcollection.values.ClassGPS;
import com.transvision.trmcollection.values.FunctionCalls;

import static android.content.Context.MODE_PRIVATE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE_FAIL;
import static com.transvision.trmcollection.values.Constants.PREFS_NAME;
import static com.transvision.trmcollection.values.Constants.sPref_DEVICE_ID;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;

public class MR_Track_Notification extends BroadcastReceiver {
    ClassGPS classGPS;
    SendingData sendingData;
    String mr_gpslat="", mr_gpslong="";
    FunctionCalls functionCalls;
    SharedPreferences settings;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MR_TRACKING_UPDATE:
                    functionCalls.logStatus("MR Tracking Successfully...");
                    break;

                case MR_TRACKING_UPDATE_FAIL:
                    functionCalls.logStatus("MR Tracking Failed...");
                    break;
            }
            return false;
        }
    });

    @Override
    public void onReceive(Context context, Intent intent) {
        classGPS = new ClassGPS(context);
        sendingData = new SendingData(context);
        functionCalls = new FunctionCalls();
        settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        GPSlocation();

        if (functionCalls.checkInternetConnection(context)) {
            if (!TextUtils.isEmpty(settings.getString(sPref_MRCODE, ""))) {
                functionCalls.logStatus("Collection MR Track DateTime: "+functionCalls.receipt_date_time());
                functionCalls.logStatus("Collection MR: "+settings.getString(sPref_MRCODE, ""));
                SendingData.MR_Track mrTrack = sendingData.new MR_Track(handler);
                mrTrack.execute(settings.getString(sPref_MRCODE, ""), settings.getString(sPref_DEVICE_ID, ""), mr_gpslong, mr_gpslat, "C");
            } else functionCalls.logStatus("No Collection MR...");
        } else functionCalls.logStatus("No Internet Connection...");
    }

    private void GPSlocation() {
        if (classGPS.canGetLocation()) {
            double latitude = classGPS.getLatitude();
            double longitude = classGPS.getLongitude();
            mr_gpslat = ""+latitude;
            mr_gpslong = ""+longitude;
        }
    }
}
