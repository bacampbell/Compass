package edu.bac.cs478.compass2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * This activity of the application is used to verify requirements needed in order to use the Camera
 * Compass mode, which displays the image from the rear-facing camera along with a compass image and
 * heading. In order to use that feature of the application, the device must have a rear-facing
 * camera and the user must give camera permissions.
 *
 * @see CameraCompassActivity
 */
public class CameraCheck extends Activity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected static final int CAMERA_PERMISSIONS = 0;
    private String backCamId;

    /**
     * Set the ContentView of the activity. Then check permissions and device for necessary
     * hardware. Start {@link CameraCompassActivity} if the device has a rear-facing camera and the
     * user has given camera permissions.
     *
     * @param savedInstanceState saved state of the activity. <code>null</code> if state has not
     *                           been saved
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //  check if the device has a camera
        if(!checkCameraHardware(this)) {
            Toast.makeText(this, "Feature not available - no camera found",
                    Toast.LENGTH_SHORT).show();

            finish();
        }
        //  camera(s) found
        else {
            //  check if camera permissions have been granted
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);

            //  user has already granted camera permissions
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                CameraManager cameraManager =
                        (CameraManager)getSystemService(Context.CAMERA_SERVICE);
                if (checkRearCam(cameraManager)) {
                    //  start camera activity
                    Intent intent = new Intent(this, CameraCompassActivity.class);
                    intent.putExtra("camID", backCamId);

                    startActivity(intent);
                }
                else {
                    Toast.makeText(this, "This feature requires a rear-facing camera",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            //  ask for required camera permissions if they haven't yet been given
            else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSIONS);
            }
        }
    }


    /**
     * To use the {@link CameraCompassActivity} feature of the application, the device requires a
     * camera, specifically a rear-facing camera. This method checks if the device has the necessary
     * hardware.
     *
     * @param context the activity's context
     * @return <code>true</code> if the device has a camera;
     *         <code>false</code> otherwise.
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    /**
     * To properly utilize the {@link CameraCompassActivity} of the application, a rear-facing
     * camera is required. This method checks if the device has one.
     *
     * @param manager instance of a {@link CameraManager}
     * @return <code>true</code> if a rear-facing camera is found;
     *         <code>false</code> otherwise.
     */
    private boolean checkRearCam(CameraManager manager) {
        boolean rearCamCheck = false;
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null &&
                        facing == CameraCharacteristics.LENS_FACING_BACK) {

                    rearCamCheck = true;
                    backCamId = cameraId;
                    break;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return rearCamCheck;
    }

    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on
     * {@link ActivityCompat#requestPermissions(Activity, String[], int)}
     *
     * @param requestCode int id of the permissions we're requesting
     * @param permissions the requested permissions
     * @param grantResults the results of asking for permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_PERMISSIONS:
                //  camera permissions granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //  make sure the device has a rear camera
                    CameraManager cameraManager =
                            (CameraManager)getSystemService(Context.CAMERA_SERVICE);

                    if (checkRearCam(cameraManager)) {
                        //  start CameraCompass activity
                        Intent intent = new Intent(this, CameraCompassActivity.class);
                        intent.putExtra("camID", backCamId);

                        startActivity(intent);
                    }
                    //  rear-facing camera was not found
                    else {
                        Toast.makeText(this, "This feature requires a rear-facing camera",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                //  camera permissions denied
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
}