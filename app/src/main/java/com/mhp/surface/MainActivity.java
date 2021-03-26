package com.mhp.surface;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Toast;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class MainActivity extends XWalkActivity {

    String url = null;

    String initContent = "<html><body><h1 style='text-align:center;font-size:10rem;'>" + Util.getHostIP() + "</h1></body></html>";

    int UI_ANIMATION_DELAY = 300;

    XWalkView mWebView;
    XWalkSettings mWebSettings;

    Handler mHideHandler = new Handler();

    ConfigHandler configHandler = new ConfigHandler();

    ConfigService configService;

    Boolean onXWalkReady = false;


    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            configService = new ConfigService(configHandler);

            configService.start();

            mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);


            SoftKeyBoardListener.setListener(MainActivity.this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
                @Override
                public void keyBoardShow(int height) {
                    // Schedule a runnable to remove the status and navigation bar after a delay
                    mHideHandler.removeCallbacks(mHideRunnable);
                    mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
                }

                @Override
                public void keyBoardHide(int height) {
                    // Schedule a runnable to remove the status and navigation bar after a delay
                    mHideHandler.removeCallbacks(mHideRunnable);
                    mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
                }
            });

            alertDialog = new AlertDialog.Builder(this).setTitle("提示信息").setMessage("NetWork DisConnected !").create();

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

            mWebView.setUIClient(new XWalkUIClient(mWebView) {

            });

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

                @Override
                public void onLoadFinished(XWalkView view, String url) {
                    if (alertDialog.isShowing()) {
                        alertDialog.hide();
                    }
                }

                @Override
                public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
                    String msg = description + "\n" + failingUrl;
                    alertDialog.setMessage(msg);
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                }
            });

            onXWalkReady = true;

            SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);

            url = sharedPreferences.getString("url", null);


            show(url);


            // turn on debugging
            XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);


            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(new WifiChangedReceiver(), filter);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void show(String url) {
        try {
            if (null == url || "".equals(url)) {

                Toast.makeText(getApplicationContext(), "Show init " + url, Toast.LENGTH_LONG).show();

                mWebView.loadDataWithBaseURL(null, initContent, "text/html", "utf-8", null);
            } else {

                Toast.makeText(getApplicationContext(), "Show " + url, Toast.LENGTH_LONG).show();

                mWebView.load(url, null);
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


    private final Runnable mHideRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            try {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mWebView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    class ConfigHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {

                url = msg.getData().getString("url");

                //步骤1：创建一个SharedPreferences对象
                SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                //步骤2： 实例化SharedPreferences.Editor对象
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //步骤3：将获取过来的值放入文件
                editor.putString("url", url);
                //步骤4：提交
                editor.commit();


                mWebView.load(url, null);
            } catch (Exception e) {
                e.printStackTrace();
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

                        Toast.makeText(context, "Wifi:" + (isConnected ? "Connected" : "DisConnected"), Toast.LENGTH_LONG).show();

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


}
