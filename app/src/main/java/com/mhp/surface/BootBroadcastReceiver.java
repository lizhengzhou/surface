package com.mhp.surface;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    static final String TAG = "lizz.AutoStarter";

    @Override
    public void onReceive(Context context, Intent srcintent) {
        try {

            Log.d(TAG, "Recieved:" + srcintent.getAction());
            if (srcintent.getAction().equals(ACTION)) {
                Log.d(TAG, "Recieved:BOOT_COMPLETED");
                Toast.makeText(context, "Booting after 30S", Toast.LENGTH_LONG).show();

                Thread.sleep(30000);

                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}