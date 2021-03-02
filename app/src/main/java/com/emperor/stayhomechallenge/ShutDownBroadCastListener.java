package com.emperor.stayhomechallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ShutDownBroadCastListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY, false);
    }
}
