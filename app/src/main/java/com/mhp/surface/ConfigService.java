package com.mhp.surface;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ConfigService extends NanoHTTPD {

    String url = null;

    MainActivity.ConfigHandler handler;

    Handler mstartHandler = new Handler();

    public ConfigService(MainActivity.ConfigHandler handler) {
        super(6421);
        this.handler = handler;
    }

    public void start() {
        try {
            mstartHandler.postDelayed(mstartRunnable, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {

        try {

            Map<String, String> parms = session.getParms();

            if (parms.containsKey("url")) {

                url = parms.get("url");

                Bundle bundle = new Bundle();

                bundle.putString("url", url);

                Message msg = new Message();

                msg.what = NotifyType.CONFIG_URL;
                msg.setData(bundle);

                handler.sendMessage(msg);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFixedLengthResponse(url);
    }


    private final Runnable mstartRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            try {
                start(10000, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
