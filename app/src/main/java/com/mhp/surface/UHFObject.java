package com.mhp.surface;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.seuic.uhf.EPC;
import com.seuic.uhf.UHFService;

import java.util.List;

public class UHFObject {

    public static final String TAG = "uhf";

    Context context;
    MainActivity.NotifyHandler notifyHandler;

    UHFService mDevice;

    EPC curEpc = null;

    public boolean mInventoryStart = false;
    private Thread mInventoryThread;

    public UHFObject(final Context context, final MainActivity.NotifyHandler notifyHandler) {
        this.context = context;
        this.notifyHandler = notifyHandler;

        mInventoryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mInventoryStart) {

                    List<EPC> mEPCList = null;
                    synchronized (context) {
                        mEPCList = mDevice.getTagIDs();
                    }
                    if (mEPCList.size() > 0) {
                        if (curEpc == null || !curEpc.equals(mEPCList.get(0))) {

                            curEpc = mEPCList.get(0);
                            Message message = Message.obtain();
                            message.what = 2;

                            Bundle bundle = new Bundle();
                            bundle.putString("tagid", curEpc.getId());

                            notifyHandler.sendMessage(message);

                        }
                    }


                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public void open() {
        mDevice = UHFService.getInstance();
        boolean ret = mDevice.open();
        if (!ret) {
            Toast.makeText(context, "读写器连接失败", Toast.LENGTH_SHORT).show();
        }

        if (mInventoryThread != null && mInventoryThread.isAlive()) {
            System.out.println("Thread not null");
            return;
        }
        if (mDevice.inventoryStart()) {
            mInventoryStart = true;
            mInventoryThread.start();

            Toast.makeText(context, "读写器打开成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "读写器打开失败", Toast.LENGTH_SHORT).show();
        }

    }

    public void close() {
        mInventoryStart = false;
        mInventoryThread.interrupt();

        if (mDevice != null) {
            mDevice.inventoryStop();
            mDevice.close();
        }
    }

}
