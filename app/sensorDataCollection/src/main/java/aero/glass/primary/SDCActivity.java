package aero.glass.primary;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Map;

import aero.glass.utils.WifiHandler;

/**
 * Created by vregath on 08/03/18.
 */

public class SDCActivity extends AeroActivity implements WifiHandler.WifiNewScanAvailableCallback {
    private WifiHandler wifiHandler;

    private float touchStartX;
    private float touchStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityStateComponent = new ActivityStateComponent(this);
        activityStateComponent.load();

        g3mComponent = new G3MComponent(this);
        g3mComponent.onCreate();

        wifiHandler = new WifiHandler(this, this);

        super.onCreate(savedInstanceState);
        for (Map<String, String> sosConnectionInfo : geoPackageHelper.getSOSConnectionInfos("custom")) {
            for (Map.Entry<String, String> entry : sosConnectionInfo.entrySet()) {
                Log.d(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        wifiHandler.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiHandler.onStart();
    }

    @Override
    protected void onPause() {
        wifiHandler.onStop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        wifiHandler.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        wifiHandler.onStop();
        super.onDestroy();
    }

    @Override
    protected void onCreateLayout() {
        g3mComponent.g3mWidget.setOnTouchListener(new View.OnTouchListener() {
            private static final long CLIKK_TIME_IN_MS = 200L;
            private static final long WAIT_TIME_IN_MS = 200L;

            private MenuDialog menuDialog;
            private long downTime;
            private long upTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (activityStateComponent.isUrbanMode()) {
                            touchStartX = event.getX();
                            touchStartY = event.getY();
                        }
                        downTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (activityStateComponent.isUrbanMode()) {
                            float x = event.getX();
                            float y = event.getY();
                            float dx = (x - touchStartX);
                            float dy = (y - touchStartY);
                            float dd = dx * 0.5f;
                            sensorComponent.cage(dd);
                            //Log.d("dedo", "YAW: " + dd + " x: " + x + " y: " + y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        final long now = System.currentTimeMillis();
                        if (now - downTime < CLIKK_TIME_IN_MS && now - upTime > WAIT_TIME_IN_MS) {
                            if (g3mComponent.isCreateVisualsDone()) {
                                if (menuDialog == null) {
                                    menuDialog = new MenuDialog(SDCActivity.this, (G3MComponent) g3mComponent);
                                    menuDialog.show();
                                } else {
                                    menuDialog.show();
                                }
                            }
                        } else {
                            if (activityStateComponent.isUrbanMode()) {
                                sensorComponent.stopCage();
                            }
                        }
                        upTime = now;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void wifiNewScanAvailable() {
        String SSID = "DIGI-01071319";
        String pass = "asrkJMCy";
        boolean connected = wifiHandler.connectIfExist(SSID, pass);
        if (connected) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }
}
