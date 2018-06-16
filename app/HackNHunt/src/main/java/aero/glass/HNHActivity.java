package aero.glass;


import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import aero.glass.primary.AeroActivity;

/**
 * Created by vregath on 08/03/18.
 */

public class HNHActivity extends AeroActivity {
    private float touchStartX;
    private float touchStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityStateComponent = new ActivityStateComponent(this);
        activityStateComponent.load();

        g3mComponent = new G3MComponent(this);
        g3mComponent.onCreate();

        super.onCreate(savedInstanceState);
    }

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
                                    menuDialog = new MenuDialog(HNHActivity.this, (G3MComponent) g3mComponent);
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
}