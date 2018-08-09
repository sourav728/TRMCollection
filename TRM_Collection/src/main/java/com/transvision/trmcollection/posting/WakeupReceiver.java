package com.transvision.trmcollection.posting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WakeupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent tracking_service = new Intent(context, MR_Tracking_service.class);
        context.startService(tracking_service);
    }
}
