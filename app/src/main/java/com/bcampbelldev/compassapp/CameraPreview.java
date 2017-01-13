package com.bcampbelldev.compassapp;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * This class is used to create and draw the camera preview used by {@link CameraCompassActivity}.
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Camera camera;


    /**
     * Class constructor.
     *
     * @param context {@link CameraCompassActivity} context
     * @param camera Our camera device
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is
        // created and destroyed.
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setKeepScreenOn(true);
    }


    /**
     * Called immediately after the surface is first created.
     *
     * @param holder {@link SurfaceHolder} whose surface is being created
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }


    /**
     * Camera preview will be released in the {@link CameraCompassActivity}.
     *
     * @param holder {@link SurfaceHolder} that holds the camera preview
     */
    public void surfaceDestroyed(SurfaceHolder holder) {}


    /**
     * Called immediately after any structural changes (format or size) have been made to the
     * surface. This method is always called at least once, after
     * {@link CameraPreview#surfaceCreated(SurfaceHolder)}.
     *
     * @param holder {@link SurfaceHolder} whose surface has changed
     * @param format {@link android.graphics.PixelFormat} of the surface
     * @param w width of the surface
     * @param h height of the surface
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.holder.getSurface() == null){
            // Preview surface does not exist.
            return;
        }

        // Start preview.
        try {
            camera.stopPreview();
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e){
            Log.d("Surface Changed", "Error starting camera preview: " + e.getMessage());
        }
    }
}