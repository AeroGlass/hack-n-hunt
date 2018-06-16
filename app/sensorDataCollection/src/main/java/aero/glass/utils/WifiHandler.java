
package aero.glass.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import aero.glass.threading.IRunnable;
import aero.glass.threading.IThread;
import aero.glass.threading.ThreadClusterFactory;

public class WifiHandler extends BroadcastReceiver {

    private Activity activity;
    private IThread startScanningThread;
    private boolean started = false;

    public WifiHandler(Activity a) {
        activity = a;
    }

    public void onReceive(Context c, Intent intent) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResult = wifiManager.getScanResults();
    }

    public void onStart() {
        Log.d("DEBUG", "WIFIHANDLER: onstart called");
        if (started) {return;}
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        activity.registerReceiver(this,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.setWifiEnabled(true);

        startScanningThread = ThreadClusterFactory.createThreadTimed(
                new StartScanningThread(), null, 5000, true);
        started = true;
    }

    private class StartScanningThread implements IRunnable {
        @Override
        public boolean onThreadStart(IThread owner) {
            return true;
        }

        @Override
        public void onThreadStop(IThread owner) {
        }

        @Override
        public boolean onThreadExecute(IThread owner) {
            WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
            Log.d("DEBUG", "WIFIHANDLER: start scan called");
            return true;
        }

        @Override
        public long getThreadRate(IThread owner) {
            return 5000;
        }

        @Override
        public String getThreadName() {
            return null;
        }

        // +2 since the G3M is showing the busy screen anyway
        @Override
        public int getThreadPriorityOffset() {
            return 0;
        }
    }

}