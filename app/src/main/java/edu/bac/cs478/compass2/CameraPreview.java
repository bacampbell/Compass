package edu.bac.cs478.compass2;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.io.IOException;
import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is
        // created and destroyed.
        holder = getHolder();
        holder.addCallback(this);
    }

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