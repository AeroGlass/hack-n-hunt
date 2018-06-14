package aero.glass.primary;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.glob3.mobile.generated.Angle;

import java.io.IOException;
import java.util.List;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private static final double ASPECT_TOLERANCE = 0.1;

    private Camera mCamera;
    private final SurfaceHolder mHolder;
    private Size mPreviewSize;
    private List<Size> mSupportedPreviewSizes;

    private boolean previewActive = false;
    private boolean hasSurf = false;

    /** Callback interface to set camera parameters. */
    interface ICameraParamSetup {
        void onSet(Camera.Parameters p);
    }

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        SurfaceView surfaceView = new SurfaceView(context, attributeSet);
        addView(surfaceView);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        // this is needed for older android versions
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private boolean prepareCamera() {
        if (mCamera == null) {
            Log.e(TAG, "set null camera!");
            return false;
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            mCamera = null;
            return false;
        }

        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        if (mSupportedPreviewSizes == null) {
            Log.e(TAG, "Failed to get supported preview sizes from camera!");
            mCamera = null;
            return false;
        }
        mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
                    getMeasuredWidth(), getMeasuredHeight());

        // try to stop everything that we don't use
        try {
            mCamera.stopSmoothZoom();
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to stop smooth zoom");
        }
        try {
            mCamera.stopFaceDetection();
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to stop face detection");
        }

        setCameraParams("preview size", new ICameraParamSetup() {
            @Override
            public void onSet(Camera.Parameters p) {
                p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            }
        });

        // focus on the view outside
        boolean focuModeSet = setCameraParams("focus mode", new ICameraParamSetup() {
            @Override
            public void onSet(Camera.Parameters p) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            }
        });
        if (!focuModeSet) {
            setCameraParams("focus mode (fixed)", new ICameraParamSetup() {
                @Override
                public void onSet(Camera.Parameters p) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                }
            });
        }

        setCameraParams("white balance", new ICameraParamSetup() {
            @Override
            public void onSet(Camera.Parameters p) {
                p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            }
        });

        setCameraParams("scene mode", new ICameraParamSetup() {
            @Override
            public void onSet(Camera.Parameters p) {
                p.setSceneMode(Camera.Parameters.SCENE_MODE_LANDSCAPE);
            }
        });

        requestLayout();
        return true;
    }

    protected boolean setCameraParams(String param, ICameraParamSetup cb) {
        Camera.Parameters parameters = mCamera.getParameters();
        cb.onSet(parameters);
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException e) {
            Log.e(TAG, "error setting camera " + param);
            return false;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);
            final int width = r - l;
            final int height = b - t;
            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }
            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height
                        / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width
                        / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width,
                        (height + scaledChildHeight) / 2);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        hasSurf = false;
        stop();
    }

    public void start(G3MComponent g3mComponent) {
        if (mCamera == null) {
            mCamera = getCameraInstance();
            if (mCamera != null) {
                prepareCamera();
            } else {
                Toast.makeText(getContext(), "Failed to open internal camera!",
                                Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                Log.e(TAG, "Failed to reconnect with camera");
                e.printStackTrace();
            }
        }

        // start prview here if it's not running AND we have a surface already
        if (mCamera != null && !previewActive && hasSurf) {
            mCamera.startPreview();
        }

        Camera.Parameters p = mCamera.getParameters();
        if (p != null) {
            float vFOV = p.getVerticalViewAngle();
            float hFOV = p.getHorizontalViewAngle();
            Log.d("CAMERA FOV V/H", vFOV + " / " + hFOV);
            g3mComponent.setupCameraFOV(Angle.fromDegrees(vFOV), Angle.fromDegrees(hFOV));
        }
    }

    public void stop() {
        if (mCamera != null) {
            if (previewActive) {
                mCamera.stopPreview();
                previewActive = false;
            }

            mCamera.release();
            mCamera = null;
        }
    }

    public boolean isActive() {
        return previewActive;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        if (sizes == null) {
            return null;
        }
        double targetRatio = (double) w / h;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mCamera != null) {
            // Now that the size is known, set up the camera parameters and
            // begin
            // the preview.

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();
            try {
                mCamera.setParameters(parameters);
            } catch (RuntimeException e) {
                Log.e(TAG, "error setting preview size", e);
            }

            mCamera.startPreview();
            previewActive = true;
        }
        hasSurf = true;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    public final Angle getHorizontalViewAngle() {
        if (mCamera == null) {
            return null;
        }
        Camera.Parameters p = mCamera.getParameters();
        if (p == null) {
            return null;
        }
        return Angle.fromDegrees(p.getHorizontalViewAngle());
    }

    public final Angle getVerticalViewAngle() {
        if (mCamera == null) {
            return null;
        }
        Camera.Parameters p = mCamera.getParameters();
        if (p == null) {
            return null;
        }
        return Angle.fromDegrees(p.getVerticalViewAngle());
    }
}
