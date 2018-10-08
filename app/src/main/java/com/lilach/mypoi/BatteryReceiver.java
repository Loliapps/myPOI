package com.lilach.mypoi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()){

            case "android.intent.action.ACTION_POWER_CONNECTED":
                Toast.makeText(context,"POWER_CONNECTED",Toast.LENGTH_SHORT).show();
                break;

            case "android.intent.action.ACTION_POWER_DISCONNECTED":
                Toast.makeText(context,"POWER_DISCONNECTED",Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
