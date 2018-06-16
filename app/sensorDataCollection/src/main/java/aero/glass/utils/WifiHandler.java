
package aero.glass.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import aero.glass.threading.IRunnable;
import aero.glass.threading.IThread;
import aero.glass.threading.ThreadClusterFactory;

public class WifiHandler extends BroadcastReceiver {

    public interface WifiNewScanAvailableCallback {
        void wifiNewScanAvailable();
    }

    // Constants used for different security types
    public static final String WPA2 = "WPA2";
    public static final String WPA = "WPA";
    public static final String WEP = "WEP";
    public static final String OPEN = "Open";
    // For EAP Enterprise fields
    public static final String WPA_EAP = "WPA-EAP";
    public static final String IEEE8021X = "IEEE8021X";
    private static final String BSSID_ANY = "any";

    public static final String[] EAP_METHOD = { "PEAP", "TLS", "TTLS" };

    public static final int WEP_PASSWORD_AUTO = 0;
    public static final int WEP_PASSWORD_ASCII = 1;
    public static final int WEP_PASSWORD_HEX = 2;
    static final String[] SECURITY_MODES = { WEP, WPA, WPA2, WPA_EAP, IEEE8021X };

    private Activity activity;
    private IThread startScanningThread;
    private boolean started = false;
    private WifiNewScanAvailableCallback wifiNewScanAvailableCallback;

    private Object wifiScanResultLock = new Object();
    private List<ScanResult> wifiScanResult = null;
    boolean claimNewScanResult = true;

    public WifiHandler(Activity a, WifiNewScanAvailableCallback cb) {
        activity = a;
        wifiNewScanAvailableCallback = cb;
    }

    public void onReceive(Context c, Intent intent) {
        if (!claimNewScanResult) {return;}
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResult = wifiManager.getScanResults();
        boolean same = true;
        if (wifiScanResult == null || scanResult.size() != wifiScanResult.size()) {
            same = false;
        }
        if (same) {
            for (int i = 0; i < scanResult.size(); i++) {
                boolean found = false;
                for (int j = 0; j < wifiScanResult.size(); j++) {
                    if (scanResult.get(i).SSID.equals(wifiScanResult.get(j).SSID)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    same = false;
                    break;
                }
            }
        }
        if (same) {return;}

        wifiScanResult = scanResult;

        claimNewScanResult = false;

        wifiNewScanAvailableCallback.wifiNewScanAvailable();

    }

    public boolean connectIfExist(String SSID, String pass) {

        ScanResult scanResult = null;
        for(int i=0;i<wifiScanResult.size();i++) {
            if (!wifiScanResult.get(i).SSID.equals(SSID)) {continue;}
            scanResult = wifiScanResult.get(i);
            break;
        }

        if (scanResult == null) {return false;}

        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = convertToQuotedString(scanResult.SSID);
        wfc.BSSID = scanResult.BSSID;

        setupSecurity(wfc, scanResult, pass);
        //TODO: should do this some smarter way?
        wfc.priority = 40;

        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration hasWfc = getWifiConfiguration(wifiManager, wfc);

        if (hasWfc == null) {
            int id = -1;
            try {
                id = wifiManager.addNetwork(wfc);
            } catch(NullPointerException e) {
                Log.e("WIFIHANDLER", "Weird!! Really!! What's wrong??", e);
                // Weird!! Really!!
                // This exception is reported by user to Android Developer Console(https://market.android.com/publish/Home)
            }
            if(id == -1) {
                return false;
            }

            if(!wifiManager.saveConfiguration()) {
                return false;
            }

            hasWfc = getWifiConfiguration(wifiManager, wfc);
            if(hasWfc == null) {
                return false;
            }
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(hasWfc.networkId, true);
        boolean connected = wifiManager.reconnect();

        return connected;
    }

    private WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final WifiConfiguration configToFind) {
        final String ssid = configToFind.SSID;
        if(ssid.length() == 0) {
            return null;
        }

        final String bssid = configToFind.BSSID;

        String security = getWifiConfigurationSecurity(configToFind);

        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();

        for(final WifiConfiguration config : configurations) {
            if(config.SSID == null || !ssid.equals(config.SSID)) {
                continue;
            }
            if(config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid == null || bssid.equals(config.BSSID)) {
                final String configSecurity = getWifiConfigurationSecurity(config);
                if(security.equals(configSecurity)) {
                    return config;
                }
            }
        }
        return null;
    }

    public String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {

        if (wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            // If we never set group ciphers, wpa_supplicant puts all of them.
            // For open, we don't set group ciphers.
            // For WEP, we specifically only set WEP40 and WEP104, so CCMP
            // and TKIP should not be there.
            if (!wifiConfig.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.CCMP)
                    &&
                    (wifiConfig.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40)
                            || wifiConfig.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104))) {
                return WEP;
            } else {
                return OPEN;
            }
        } else if (wifiConfig.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
            return WPA2;
        } else if (wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)) {
            return WPA_EAP;
        } else if (wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return IEEE8021X;
        } else if (wifiConfig.allowedProtocols.get(WifiConfiguration.Protocol.WPA)) {
            return WPA;
        } else {
            Log.w("WIFIHANDLER", "Unknown security type from WifiConfiguration, falling back on open.");
            return OPEN;
        }
    }

    private void setupSecurity(WifiConfiguration wfc, ScanResult scanResult, String pass) {
        wfc.allowedAuthAlgorithms.clear();
        wfc.allowedGroupCiphers.clear();
        wfc.allowedKeyManagement.clear();
        wfc.allowedPairwiseCiphers.clear();
        wfc.allowedProtocols.clear();

        final String cap = scanResult.capabilities;
        String securityMode = OPEN;
        for (int i = SECURITY_MODES.length - 1; i >= 0; i--) {
            if (cap.contains(SECURITY_MODES[i])) {
                securityMode = SECURITY_MODES[i];
                break;
            }
        }

        if (securityMode.equals(WEP)) {
            int wepPasswordType = WEP_PASSWORD_AUTO;
            // If password is empty, it should be left untouched
            if (pass != null && !pass.isEmpty()) {
                if (wepPasswordType == WEP_PASSWORD_AUTO) {
                    if (isHexWepKey(pass)) {
                        wfc.wepKeys[0] = pass;
                    } else {
                        wfc.wepKeys[0] = convertToQuotedString(pass);
                    }
                } else {
                    wfc.wepKeys[0] = wepPasswordType == WEP_PASSWORD_ASCII
                            ? convertToQuotedString(pass)
                            : pass;
                }
            }

            wfc.wepTxKeyIndex = 0;

            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        } else if (securityMode.equals(WPA) || securityMode.equals(WPA2)){
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

            wfc.allowedProtocols.set(securityMode.equals(WPA2) ?
                    WifiConfiguration.Protocol.RSN : WifiConfiguration.Protocol.WPA);

            // If password is empty, it should be left untouched
            if (pass != null && !pass.isEmpty()) {
                if (pass.length() == 64 && isHex(pass)) {
                    // Goes unquoted as hex
                    wfc.preSharedKey = pass;
                } else {
                    // Goes quoted as ASCII
                    wfc.preSharedKey = convertToQuotedString(pass);
                }
            }

        } else if (securityMode.equals(OPEN)) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (securityMode.equals(WPA_EAP) || securityMode.equals(IEEE8021X)) {
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            if (securityMode.equals(WPA_EAP)) {
                wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            } else {
                wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
            }
            if (pass != null && !pass.isEmpty()) {
                wfc.preSharedKey = convertToQuotedString(pass);
            }
        }
    }

    private String convertToQuotedString(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if(lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    private boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }

        return true;
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

    public void onStop() {
        Log.d("DEBUG", "WIFIHANDLER: onstop called");
        if (!started) {return;}
        startScanningThread.stopThread(6000);
        activity.unregisterReceiver(this);
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        //wifiManager.setWifiEnabled(false);
        started = false;
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