package com.bcampbelldev.compassapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * MainActivity of the application. It will display a compass image and the heading based on the
 * orientation of the device compared to magnetic north.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    protected float[] rotationMatrixFromEvent = new float[16];
    protected float[] transformedRotationMatrix = new float[16];
    protected float[] orientation = new float[3];
    protected float currentDegree = 0f;

    private TextView headingView;
    private ImageView compassView;


    /**
     * Set the ContentView for the activity and initialize the App Bar, the two Views of the
     * activity, and the SensorManager.
     *
     * @param savedInstanceState saved state of the activity. <code>null</code> if state has not
     *                           been saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(myToolbar);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        compassView = (ImageView)findViewById(R.id.imageViewCompass);
        headingView = (TextView)findViewById(R.id.heading);
    }


    /**
     * Register the rotation vector sensor listener and display dialog if not previously dismissed.
     */
    @Override
    protected void onResume() {
        // Determine if the user has opted to not show the dialog.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean dialog_status = prefs.getBoolean("dialog_status", false);

        // Show the dialog and save the user's selection on whether or not to show it again.
        if (!dialog_status) {
            View dialog = getLayoutInflater().inflate(R.layout.dialog, null);
            final CheckBox userCheck = (CheckBox) dialog.findViewById(R.id.dialog_layout);

            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title)
                    .setView(dialog)
                    .setPositiveButton(R.string.got_it_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(MainActivity.this);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("dialog_status", userCheck.isChecked());
                            editor.apply();
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        // Remind the user to keep their device screen parallel to the ground
        String reminder = "Keep device screen parallel to the ground";
        Toast.makeText(this, reminder, Toast.LENGTH_LONG).show();

        super.onResume();

        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }


    /**
     * Unregister the sensor listener to save battery.
     */
    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
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
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){

            // Helper function to convert a rotation vector to a rotation matrix: converts device
            // coordinate to world coordinate.
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromEvent, event.values);

            // Rotates the given matrix (rotationMatrixFromEvent) so that it is expressed in a
            // different coordinate system.
            SensorManager.remapCoordinateSystem(
                    rotationMatrixFromEvent,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    transformedRotationMatrix);

            // Compute the three orientation angles of the device (in radians):
            //    azimuth = orientation[0]
            //    pitch = orientation[1]
            //    roll = orientation[2]
            SensorManager.getOrientation(transformedRotationMatrix, orientation);

            // Get the azimuth in degrees and set the heading TextView. The azimuth (in this
            // orientation, i.e. the device held flat in portrait mode with the screen facing
            // upwards and parallel to the ground) is the angle between the y-axis and magnetic
            // north, rotated around the z-axis.
            float degree = (float)Math.toDegrees(orientation[0]);
            degree = Math.round(degree);

            // Azimuth ranges from -180 to 180. Convert to 0-360 range to match compass image.
            if (degree < 0) {
                degree += 360;
            }

            String heading = String.format("%1$d\u00B0", (int)degree);
            headingView.setText(heading);

            // Create a rotation animation.
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            // How long the animation will take place.
            ra.setDuration(210);

            // Set the animation after the end of the reservation status.
            ra.setFillAfter(true);

            // Start the animation.
            compassView.startAnimation(ra);

            // Update the new current degree.
            currentDegree = -degree;
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
     * Initialize the contents of the Activity's options menu.
     *
     * @param menu the options {@link Menu} that will contain the menu items
     * @return <code>true</code> if you want to display the menu;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Start {@link CameraCompassActivity} on user selection in the App Bar.
     *
     * @param item {@link MenuItem} item that the user selected.
     * @return <code>true</code> on code completion on item selection;
     *         <code>false</code> by default.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera_menu_item:

                // Start the CameraCompass activity.
                Intent intent = new Intent(this, CameraCompassActivity.class);
                startActivity(intent);

                return true;

            default:
                return false;
        }
    }
}