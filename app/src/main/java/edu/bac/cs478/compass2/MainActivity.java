package edu.bac.cs478.compass2;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    private float[] rotationMatrixFromEvent = new float[16];
    private float[] transformedRotationMatrix = new float[16];
    private float[] orientation = new float[3];

    private TextView headingView;
    private ImageView compassView;
    private float currentDegree = 0f;

    /***********************************************************************************************
                     onCreate: initialize SensorManager and Views
     **********************************************************************************************/
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

        //  display Toast: compass is meant to be held with the device screen parallel to the ground
        Toast.makeText(this, "Keep device screen parallel to the ground", Toast.LENGTH_LONG).show();
    }

    /***********************************************************************************************
                     onResume: register the rotation vector sensor
     **********************************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
    }

    /***********************************************************************************************
                     onPause
     **********************************************************************************************/
    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    /***********************************************************************************************
                     onSensorChanged
     **********************************************************************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {
        //  only do work on rotation vector sensor events
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){

            /*
                helper function to convert a rotation vector to a rotation matrix: converts device
                coordinate to world coordinate
            */
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromEvent, event.values);

            /*
                rotates the given matrix (rotationMatrixFromEvent) so that it is expressed in a
                different coordinate system
            */
            SensorManager.remapCoordinateSystem(
                    rotationMatrixFromEvent,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    transformedRotationMatrix);

            /*
                compute the three orientation angles of the device (in radians):
                  azimuth = orientation[0]
                  pitch = orientation[1]
                  roll = orientation[2]
            */
            SensorManager.getOrientation(transformedRotationMatrix, orientation);

            //  get the azimuth in degrees and set heading TextView
            float degree = (float)Math.toDegrees(orientation[0]);
            degree = Math.round(degree);

            //  azimuth ranges from -180 to 180. Convert to 0-360 to match compass image
            if(degree < 0) {
                degree = 360 + degree;
            }
            headingView.setText("Heading: " + Float.toString(degree) + " degrees");

            //  create a rotation animation
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            //  how long the animation will take place
            ra.setDuration(210);

            //  set the animation after the end of the reservation status
            ra.setFillAfter(true);

            //  start the animation
            compassView.startAnimation(ra);
            currentDegree = degree;
        }
    }

    /***********************************************************************************************
                    onAccuracyChanged: needed for SensorEventListener interface (not used)
     **********************************************************************************************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /***********************************************************************************************
     create the Action Bar overflow menu
     **********************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //  TO-DO: camera functionality
            case R.id.camera_menu_item:
                //Intent intent = new Intent(this, CameraActivity.class);
                //startActivity(intent);
                return true;

            default:
                return true;
        }
    }
}
