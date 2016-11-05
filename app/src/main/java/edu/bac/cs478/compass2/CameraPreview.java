package edu.bac.cs478.compass2;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
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
            camera.startPreview();
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

        // Stop preview before making changes.
        try {
            camera.stopPreview();
        } catch (Exception e){
            // Ignore: tried to stop a non-existent preview.
        }

        // Start preview.
        try {
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}