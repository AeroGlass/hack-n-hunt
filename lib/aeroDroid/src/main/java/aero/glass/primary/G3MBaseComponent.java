package aero.glass.primary;

import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.util.Log;
import android.widget.Toast;

import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.G3MContext;
import org.glob3.mobile.generated.GInitializationTask;
import org.glob3.mobile.generated.GPUProgramSources;
import org.glob3.mobile.generated.GTask;
import org.glob3.mobile.generated.IPrePostRenderTasks;
import org.glob3.mobile.generated.IThreadUtils;
import org.glob3.mobile.generated.LabelImageBuilder;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import aero.glass.renderer.textrender.AeroTextCoreAndroid;
import aero.glass.renderer.textrender.EFontTypes;
import aero.glass.renderer.textrender.TextRenderer;
import aero.glass.threading.IRunnable;
import aero.glass.threading.IThread;
import aero.glass.threading.ThreadClusterFactory;
import aero.glass.unit.AHLR;
import aero.glass.utils.FileIOHelper;
import aero.glass.utils.G3MHelper;

/**
 * Created by zolta on 2018. 04. 15..
 */

public abstract class G3MBaseComponent {
    private static final String TAG = "G3MComponent";

    public G3MWidget_Android g3mWidget;
    private IThreadUtils threadUtils;
    protected LabelImageBuilder progLabel;
    private volatile boolean bStartupDone;
    protected final AeroActivity activity;
    private volatile AHLR ahlr = null;
    public volatile boolean bCreateVisualsDone = false;

    protected abstract void onAsyncinit();
    protected abstract boolean onSyncInit(G3MBuilder_Android builder);
    public abstract void onPreRenderTask();

    /** Asynchronous initialization thread used on startup. */
    protected class InitThread implements IRunnable {
        @Override
        public boolean onThreadStart(IThread owner) {
            onAsyncinit();
            return false;
        }

        @Override
        public void onThreadStop(IThread owner) {
        }

        @Override
        public boolean onThreadExecute(IThread owner) {
            return false;
        }

        @Override
        public long getThreadRate(IThread owner) {
            return 0;
        }

        @Override
        public String getThreadName() {
            return null;
        }

        // +2 since the G3M is showing the busy screen anyway
        @Override
        public int getThreadPriorityOffset() {
            return 2;
        }
    }

    /**
     * Class used to hook into the rendering cycle and calling the onUpdate function
     * on start of every frame.
     */
    public class PrePostRenderTasks extends IPrePostRenderTasks {
        private long lastRender = System.currentTimeMillis();

        @Override
        public void preRenderTask() {
            if (bStartupDone && ahlr != null) {
                onPreRenderTask();
                G3MHelper.setCamera(ahlr, g3mWidget.getG3MWidget(), null);
                Long now = System.currentTimeMillis();
                while (now - lastRender < 30) {
                    synchronized (this) {
                        try {
                            wait((now - lastRender) / 2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    now = System.currentTimeMillis();
                }
                lastRender = now;
            }
        }

        @Override
        public void postRenderTask() {
        }
    }

    /**
     * Asynchronous initialization task used on startup.
     * @return GInitializationTask object for the task
     */
    private GInitializationTask getStartupTask() {
        return new GInitializationTask() {
            private IThread startupThread;

            /**
             * This function is called from the G3M render thread so updates or
             * changes related to the G3M context can be done safely here.
             *
             * IMPORTANT : here this is a blocking call so do not block for too
             * long otherwise the app may look like its frozen right on the
             * startup!
             */
            @Override
            public boolean isDone(G3MContext context) {
                final boolean bResult = startupThread.waitForCompletion(30);
                if (bResult) {
                    bStartupDone = true;
                    g3mWidget.getG3MContext().getEffectsScheduler().cancelAllEffectsFor(
                            g3mWidget.getG3MWidget().getNextCamera().getEffectTarget());
                    ThreadClusterFactory.destroyThread(startupThread);
                    startupThread = null;
                    initTextRenderer();
                }
                return bResult;
            }

            @Override
            public void run(G3MContext context) {
                startupThread = ThreadClusterFactory.createThreadOneShot(new InitThread());
            }
        };
    }

    protected G3MBaseComponent(AeroActivity ca) {
        activity = ca;
    }

    public void onCreate() {
        bStartupDone = false;

        // G3M essentials + blocking / important initializers
        final G3MBuilder_Android builder = new G3MBuilder_Android(activity, true);
        builder.setStorageExternalThreshold(1024 * 1024); // 1 MiB
        if (!onSyncInit(builder)) {
            final String gm3FailMsg = "Failed to do syncronous initialization";
            Log.e(TAG, "::onCreate , " + gm3FailMsg);
            showToast(gm3FailMsg, Toast.LENGTH_LONG);
            activity.finish();
            return;
        }

        loadGPUShadersForG3MBuilder(builder);

        // The task is created by G3M when the renderer starts
        builder.setInitializationTask(getStartupTask());
        g3mWidget = builder.createWidget();

        g3mWidget.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        if (g3mWidget.getG3MWidget().getPlanetRenderer() != null) {
            g3mWidget.getG3MWidget().getPlanetRenderer().setRenderTileMeshes(false);
        }
        threadUtils = g3mWidget.getG3MWidget().getG3MContext().getThreadUtils();

        // Because we use camera underlay!
        g3mWidget.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        g3mWidget.getG3MWidget().setBackgroundColor(Color.transparent());
    }

    protected void onStart() {
        if (g3mWidget != null && threadUtils != null) {
            // TODO: this is a hack so that the thread utils will call drain it's quene
            // calling on resume on the thread utils is okay, because the assumption is that
            // the the G3M internals will call onStart as well
            threadUtils.onResume(g3mWidget.getG3MContext());
            threadUtils.invokeInRendererThread(new GTask() {
                @Override
                public void run(G3MContext context) {
                    g3mWidget.getG3MWidget().onResume();
                }
            }, false);
        }
    }

    protected void onStop() {
        if (g3mWidget != null && threadUtils != null) {
            threadUtils.invokeInRendererThread(new GTask() {
                @Override
                public void run(G3MContext context) {
                    g3mWidget.getG3MWidget().onPause();
                }
            }, false);
        }
    }

    protected void onDestroy() {
        if (g3mWidget != null) {
            g3mWidget.getG3MWidget().onDestroy();
        }
    }

    /** This function is exclusively for updating the startup screen progress text.
     *  @param newText new text to be displayed
     * */
    protected final void updateStartupProgressText(final String newText) {
        if (isStartupDone() || progLabel == null) {
            return;
        }
        threadUtils.invokeInRendererThread(new GTask() {
            @Override
            public void run(G3MContext context) {
                progLabel.setText(newText);
            }
        }, true);
    }

    protected void runOnRendererThread(final Runnable r) {
        threadUtils.invokeInRendererThread(new GTask() {
            @Override
            public void run(G3MContext context) {
                r.run();
            }
        }, true);
    }

    protected final void runOnUiThread(Runnable action) {
        activity.runOnUiThread(action);
    }

    protected final void showToast(final String text, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Toast t = Toast.makeText(activity, text, duration);
                t.show();
            }
        });
    }

    public synchronized void setAHLR(AHLR ahlr) {
        this.ahlr = ahlr;
    }

    public synchronized AHLR getAHLR() {
        return ahlr;
    }

    public boolean isStartupDone() {
        return bStartupDone;
    }

    /**
     * Enable processing of touch events by G3M.
     * @param touch to let G3M handle touch events
     */
    public void setTouchMode(boolean touch) {
        g3mWidget.getCameraRenderer().setProcessTouchEvents(touch);
    }

    protected void setupCameraFOV(final Angle v, final Angle h) {
        runOnRendererThread(new Runnable() {
            @Override
            public void run() {
                //g3mWidget.getG3MWidget().getNextCamera().setFOV(v, h);
                Camera nextCamera = g3mWidget.getG3MWidget().getNextCamera();
                Log.d("G3M FOV V/H",
                        nextCamera.getVerticalFOV() + " / " + nextCamera.getHorizontalFOV());
            }
        });
    }

    private void initTextRenderer() {
        threadUtils.invokeInRendererThread(new GTask() {
            @Override
            public void run(G3MContext context) {
                AeroTextCoreAndroid tCore = new AeroTextCoreAndroid(activity.getAssets());
                tCore.load(EFontTypes.LABEL_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFFFFFFFF, 0x00000000, 0xFF000000);
                tCore.load(EFontTypes.HUD_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFF64E324, 0x00000000, 0x00000000);
                tCore.load(EFontTypes.TRAFFIC_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFF70D0FF, 0x00000000, 0x00000000);
                tCore.load(EFontTypes.THREAT_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFFFF0F0F, 0x00000000, 0x00000000);
                tCore.load(EFontTypes.DEBUG_GREEN_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFF00E000, 0x00000000, 0xFF000000);
                tCore.load(EFontTypes.DEBUG_RED_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFFFF0000, 0x00000000, 0xFF000000);
                tCore.load(EFontTypes.DEBUG_MAGENTA_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFFFF00FF, 0x00000000, 0xFF000000);
                tCore.load(EFontTypes.DEBUG_CYAN_FONT_ID, "fonts/Roboto-Regular.ttf",
                        0xFF00FFFF, 0x00000000, 0xFF000000);
                TextRenderer.setTextRenderCore(tCore);
            }
        }, true);
    }

    /**
     * Load GPU shaders from the application's external storage directory from
     * the subdirectory 'shaders'.
     */
    private void getGPUShadersFromExternalStorage(HashMap<String, GPUProgramSources> storage) {
        final File extDir = activity.getExternalFilesDir(null);
        if (extDir == null || !extDir.exists()) {
            return;
        }

        final File shaderDir = new File(extDir.getPath() + File.separatorChar + "shaders");
        if (!shaderDir.exists()) {
            return;
        }

        final String[] shaderList = shaderDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".fsh");
            }
        });

        if (shaderList != null && shaderList.length > 0) {
            for (String path : shaderList) {
                final String[] fsl = path.split("\\.");
                if (fsl.length > 0 && fsl[0] != null) {

                    boolean found = false;
                    for (Map.Entry<String, GPUProgramSources> src : storage.entrySet()) {
                        if (fsl[0].equalsIgnoreCase(src.getKey())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    final String fshSrc = FileIOHelper.readStringFromFile(
                            new File(shaderDir, fsl[0] + ".fsh"));
                    if (fshSrc == null || fshSrc.isEmpty()) {
                        Log.e("shader", "Failed to load fragment shader source of : " + fsl[0]);
                        continue;
                    }

                    final String vshSrc = FileIOHelper.readStringFromFile(
                            new File(shaderDir, fsl[0] + ".vsh"));
                    if (vshSrc == null || vshSrc.isEmpty()) {
                        Log.e("shader", "Failed to load vertex shader source of : " + fsl[0]);
                        continue;
                    }

                    storage.put(fsl[0], new GPUProgramSources(fsl[0], vshSrc, fshSrc));
                    Log.d("shader", "Loaded shader program from external storage : " + fsl[0]);
                }
            }
        }
    }

    /** Load GPU shaders from the APK's assets. */
    private void getGPUShadersFromAssets(HashMap<String, GPUProgramSources> storage)
            throws IOException {
        final AssetManager a = activity.getAssets();
        final String[] shaderList = a.list("shaders");

        for (String srcFile : shaderList) {
            if (srcFile.endsWith(".fsh")) {
                final String[] fsl = srcFile.split("\\.");
                if (fsl.length > 0 && fsl[0] != null) {

                    boolean found = false;
                    for (Map.Entry<String, GPUProgramSources> src : storage.entrySet()) {
                        if (fsl[0].equalsIgnoreCase(src.getKey())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    final String fshSrc = FileIOHelper
                            .getStringFromAssets(activity, "shaders"
                                    + File.separatorChar + fsl[0] + ".fsh");
                    if (fshSrc == null || fshSrc.isEmpty()) {
                        Log.e("shader", "Failed to load fragment shader source of : " + fsl[0]);
                        continue;
                    }

                    final String vshSrc = FileIOHelper
                            .getStringFromAssets(activity, "shaders"
                                    + File.separatorChar + fsl[0] + ".vsh");
                    if (vshSrc == null || vshSrc.isEmpty()) {
                        Log.e("shader", "Failed to load vertex shader source of : " + fsl[0]);
                        continue;
                    }

                    storage.put(fsl[0], new GPUProgramSources(fsl[0], vshSrc, fshSrc));
                    Log.d("shader", "Loaded shader program from assets : " + fsl[0]);
                }
            }
        }
    }

    /**
     * Loads GPU shader sources and adds them to the widget builder.
     */
    protected void loadGPUShadersForG3MBuilder(G3MBuilder_Android builder) {
        final HashMap<String, GPUProgramSources> gpuPrograms =
                new HashMap<String, GPUProgramSources>();

        // TODO: loading shader sources from external storage is for development only
        getGPUShadersFromExternalStorage(gpuPrograms);

        try {
            getGPUShadersFromAssets(gpuPrograms);
        } catch (IOException e) {
            Log.e("shader", "Failed to read shaders from assets, IOException");
            e.printStackTrace();
        }

        int count = 0;
        for (GPUProgramSources s : gpuPrograms.values()) {
            builder.addGPUProgramSources(s);
            count++;
        }
        if (count > 0) {
            Log.i("shader", "Added " + count + " additional GPU program sources");
        }
    }

    public boolean isCreateVisualsDone() {
        return bCreateVisualsDone;
    }
}
