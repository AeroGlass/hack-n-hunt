package aero.glass.primary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import aero.glass.aerodroid.R;
import aero.glass.unit.Geoid;
import aero.glass.utils.GeoPackageHelper;

/**
 * Created by vregath on 08/03/18.
 */

public abstract class AeroActivity extends Activity {
    public static final boolean DEMO_MODE = false;
    public static final boolean PLANET = false;

    public G3MBaseComponent g3mComponent;
    public SensorComponent sensorComponent;
    public ActivityStateBaseComponent activityStateComponent;
    public GeoPackageHelper geoPackageHelper;

    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        geoPackageHelper = new GeoPackageHelper(this);

        Geoid.init(this);

        createLayout();
        sensorComponent = new SensorComponent(this, g3mComponent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        g3mComponent.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.start(g3mComponent);
        sensorComponent.onResume();
    }

    @Override
    protected void onPause() {
        sensorComponent.onPause();
        cameraPreview.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (g3mComponent.isStartupDone()) {
            g3mComponent.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityStateComponent.save();
        g3mComponent.onDestroy();
        super.onDestroy();
    }

    protected abstract void onCreateLayout();

    @SuppressLint("ClickableViewAccessibility")
    private void createLayout() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE); make crash (Sony xperia z1 compact)

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setImmersive(true);

        setContentView(R.layout.activity_main);

        final FrameLayout fl = new FrameLayout(this);
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        fl.setLayoutParams(lp);

        final View bgFill = new View(this);
        bgFill.setLayoutParams(lp);
        bgFill.setBackgroundColor(Color.BLACK);

        fl.addView(bgFill);

        cameraPreview = new CameraPreview(this, null);
        fl.addView(cameraPreview, 1);

        fl.addView(g3mComponent.g3mWidget, 2);

        g3mComponent.g3mWidget.bringToFront();
        g3mComponent.g3mWidget.setZOrderMediaOverlay(true);

        final LinearLayout ll = (LinearLayout) findViewById(R.id.glob3);

        ll.addView(fl);

        onCreateLayout();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public boolean shouldUseStereo() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        sensorComponent.resetCage();
    }
}
