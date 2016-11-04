package edu.bac.cs478.compass2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import static edu.bac.cs478.compass2.CameraCheck.CAMERA_PERMISSIONS;

/**
 * This activity of the application allows the user to see their heading through their camera.
 * It could be useful in the event that the user needs to follow a certain heading and wants to find
 * a landmark along that heading.
 */
@SuppressWarnings("deprecation")
public class CameraCompassActivity extends Activity implements SensorEventListener {
    private Camera camera;
    private int backCamId = 0;

    private SensorManager mSensorManager;

    private float[] rotationMatrixFromEvent = new float[16];
    private float[] transformedRotationMatrix = new float[16];
    private float[] orientation = new float[3];

    private TextView headingView;


    /**
     * For this activity to work the device must have a camera, specifically a rear-facing camera.
     * A camera check is done using {@link CameraCompassActivity#checkCameraHardware(Context)} and a
     * rear-facing camera is detected using {@link CameraCompassActivity#getBackCamId()}. If a
     * camera is found, the user must give camera permissions to use it.
     *
     * @param savedInstanceState saved state of the activity. <code>null</code> if state has not
     *                           been saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_layout);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Check if the device has a camera.
        if(!checkCameraHardware(this)) {
            Toast.makeText(this, "This device does not have a camera", Toast.LENGTH_SHORT).show();
            finish();
        }
        // If the device has a camera:
        else {
            // Check if camera permissions have been granted.
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);

            // User has already granted camera permissions.
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // Check if the device has a rear-facing camera.
                backCamId = getBackCamId();
                if (backCamId < 0) {
                    Toast.makeText(this, "This feature requires a rear-facing camera",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
                // If the device has a rear-facing camera and camera permissions have been granted,
                // open a connection to the camera.
                try {
                    releaseCamera();
                    camera = Camera.open(backCamId);
                    createPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Ask for required camera permissions if they haven't yet been given.
            else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSIONS);
            }
        }

        // Setup the heading View and SensorManager.
        headingView = (TextView)findViewById(R.id.augReality_heading);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }


    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on
     * {@link ActivityCompat#requestPermissions(Activity, String[], int)}
     *
     * @param requestCode int id of the permissions we're requesting
     * @param permissions the requested permissions
     * @param grantResults the results of asking for permissions
     *
     * @see CameraCompassActivity#onCreate(Bundle)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_PERMISSIONS:
                // Camera permissions granted.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Make sure the device has a rear-facing camera.
                    backCamId = getBackCamId();
                    if (backCamId < 0) {
                        Toast.makeText(this, "This feature requires a rear-facing camera",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    }
                    // If the device has a rear-facing camera, open a connection to it.
                    try {
                        releaseCamera();
                        camera = Camera.open(backCamId);
                        createPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Camera permissions denied.
                else {
                    Toast.makeText(this, "This feature is unavailable without camera permissions",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            default:
                break;
        }
    }


    /**
     * Check the device for a camera.
     *
     * @param context Activity context
     * @return <code>true</code> If a camera is found;
     *         <code>false</code> otherwise.
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    /**
     * Release the camera immediately if the activity goes out of focus. Stop the sensor listener to
     * conserve battery.
     */
    @Override
    protected void onPause() {
        releaseCamera();
        mSensorManager.unregisterListener(this);
        super.onPause();
    }


    /**
     * Register a motion sensor listener of type rotation vector. This is how we determine the
     * device's orientation.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
    }


    /**
     * Release the camera when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }


    /**
     * Method to release the camera object.
     */
    private void releaseCamera() {
        //camPreview = null;
        if (camera != null){
            camera.release();
            camera = null;
        }
    }


    /**
     * Method to determine the ID of the rear-facing camera. Loops through the device's cameras
     * and checks their orientation using {@link CameraInfo}.
     *
     * @return cameraID the integer ID of the rear-facing camera, or -1 if the device does not have
     *                  a rear-facing camera
     */
    private int getBackCamId() {
        int cameraID = -1;
        int numCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraID = i;
                break;
            }
        }

        return cameraID;
    }


    /**
     * Called on new reading from sensor. We will use the sensor data to determine the orientation
     * of the device.
     *
     * @param event holds information such as the sensor's type, the time-stamp, accuracy, and
     *              the sensor's data
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Only do work on rotation vector sensor events.
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            // Helper function to convert a rotation vector to a rotation matrix: converts device
            // coordinate to world coordinate.
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromEvent, event.values);

            // Rotates the given matrix (rotationMatrixFromEvent) so that it is expressed in a
            // different coordinate system.
            SensorManager.remapCoordinateSystem(
                    rotationMatrixFromEvent,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    transformedRotationMatrix);

            // Compute the three orientation angles of the device (in radians):
            //    azimuth = orientation[0]
            //    pitch = orientation[1]
            //    roll = orientation[2]
            SensorManager.getOrientation(transformedRotationMatrix, orientation);

            // Get the azimuth in degrees and set the heading TextView. The azimuth (in this
            // orientation, i.e. the device in landscape mode with the screen perpendicular to the
            // ground and facing the user) is the angle between the z-axis and magnetic north,
            // rotated around the y-axis.
            float degree = (float) Math.toDegrees(orientation[0]);
            degree = Math.round(degree);

            // Azimuth ranges from -180 to 180. Convert to 0-360 range.
            if (degree < 0) {
                degree += 360;
            }

            headingView.setText("Heading: " + Float.toString(degree) + " degrees");
        }
    }


    /**
     * Required by {@link SensorEventListener}, but not used here.
     *
     * @param sensor ID of the sensor being monitored
     * @param accuracy new accuracy of the sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    /**
     * Method used to create the camera preview and add it to the layout's view.
     */
    public void createPreview() {
        CameraPreview camPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(camPreview);
    }
}