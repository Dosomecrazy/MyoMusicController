/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.christophervalles.android.myo_music_controller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";

    private Toast mToast;
    private boolean unlocked = false;
    private boolean unlocked1 = false;

    Handler handler = new Handler();

    Myo globalMyo;

    Runnable lockMyo = new Runnable(){
        @Override
        public void run(){
            unlocked = false;
            unlocked1 = false;
            globalMyo.vibrate(Myo.VibrationType.SHORT);
            globalMyo.vibrate(Myo.VibrationType.SHORT);
        }
    };

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            globalMyo = myo;
            showToast(getString(R.string.connected));
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            showToast(getString(R.string.disconnected));
        }

        // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
        // making a fist, or not making a fist anymore.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            if (unlocked) {
                if (pose.equals(Pose.FINGERS_SPREAD) == true) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "togglepause");
                    sendBroadcast(i);
                    extendLock();
                } else if (pose.equals(Pose.WAVE_IN) == true) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "previous");
                    sendBroadcast(i);
                    extendLock();
                } else if (pose.equals(Pose.WAVE_OUT) == true) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "next");
                    sendBroadcast(i);
                    extendLock();
                }
            } else if (!unlocked && !unlocked1 && pose.equals(Pose.THUMB_TO_PINKY)) {
                unlocked1 = true;
            } else if (!unlocked && unlocked1) {
                if (pose.equals(Pose.FIST)) {
                    unlocked = true;
                    myo.vibrate(Myo.VibrationType.SHORT);

                    handler.postDelayed(lockMyo, 2000);
                } else if(!pose.equals(Pose.REST)) {
                    unlocked1 = false;
                }
            }
        }

        public void extendLock() {
            handler.removeCallbacks(lockMyo);
            handler.postDelayed(lockMyo, 2000);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            showToast("Couldn't initialize Hub");
            stopSelf();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        // Finally, scan for Myo devices and connect to the first one found.
        hub.pairWithAnyMyo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Service is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        Hub.getInstance().shutdown();
    }

    private void showToast(String text) {
        Log.w(TAG, text);
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }
}
