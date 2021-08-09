package com.mhp.surface;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebSettings;
import android.widget.Toast;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

public class MainActivity extends XWalkActivity {

    String _url = null;

    String initContent = "<html><body><h1 style='text-align:center;font-size:10rem;'>" + Util.getHostIPhtml() + "</h1></body></html>";

    XWalkView mWebView;
    XWalkSettings mWebSettings;

    ConfigHandler configHandler = new ConfigHandler();

    ConfigService configService;

    Boolean onXWalkReady = false;

    WifiChangedReceiver wifiChangedReceiver = new WifiChangedReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            configService = new ConfigService(configHandler);

            configService.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onXWalkReady() {

        try {
            mWebView = (XWalkView) findViewById(R.id.xwalkWebView);

            mWebSettings = mWebView.getSettings();

            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setUseWideViewPort(true);
            mWebSettings.setLoadWithOverviewMode(true);
            mWebSettings.setDomStorageEnabled(true);


            mWebView.setResourceClient(new XWalkResourceClient(mWebView) {
                @Override
                public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
                    //Android 8.0以下版本的需要返回true 并且需要loadUrl()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }
            });

            onXWalkReady = true;

            SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);

            _url = sharedPreferences.getString("url", _url);


            show(_url);


            // turn on debugging
            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);


            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(wifiChangedReceiver, filter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void show(String url) {
        try {
            if (null == url || "".equals(url)) {

                Toast.makeText(getApplicationContext(), "Show init " + url, Toast.LENGTH_SHORT).show();

                mWebView.loadData(initContent, "text/html", "utf-8");
            } else {

                Toast.makeText(getApplicationContext(), "Show " + url, Toast.LENGTH_SHORT).show();

                mWebView.loadUrl(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mWebView != null) {
                mWebView.pauseTimers();
                mWebView.onHide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mWebView != null) {
                mWebView.resumeTimers();
                mWebView.onShow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(wifiChangedReceiver);
            if (mWebView != null) {
                mWebView.onDestroy();
            }
            if (configService != null) {
                configService.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class ConfigHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what) {

                case NotifyType.CONFIG_URL:

                    try {

                        _url = msg.getData().getString("url");

                        //步骤1：创建一个SharedPreferences对象
                        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                        //步骤2： 实例化SharedPreferences.Editor对象
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        //步骤3：将获取过来的值放入文件
                        editor.putString("url", _url);
                        //步骤4：提交
                        editor.commit();

                        mWebView.clearCache(true);

                        show(_url);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }


    class WifiChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                // 监听wifi的连接状态即是否连上了一个有效无线路由
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    Parcelable parcelableExtra = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (null != parcelableExtra) {
                        // 获取联网状态的NetWorkInfo对象
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        //获取的State对象则代表着连接成功与否等状态
                        NetworkInfo.State state = networkInfo.getState();
                        //判断网络是否已经连接
                        boolean isConnected = state == NetworkInfo.State.CONNECTED;
                        Log.e("TAG", "isConnected:" + isConnected);

                        if (isConnected) {
                            show(_url);
                        }

                        Toast.makeText(context, "Wifi:" + (isConnected ? "Connected" : "DisConnected"), Toast.LENGTH_SHORT).show();

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


}
