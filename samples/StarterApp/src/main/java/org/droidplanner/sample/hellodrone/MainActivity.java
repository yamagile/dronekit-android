package org.droidplanner.sample.hellodrone;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.android.client.utils.video.DecoderListener;
import com.o3dr.android.client.utils.video.MediaCodecManager;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.companion.solo.SoloState;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.io.IOException;
import java.util.List;

import static com.o3dr.android.client.apis.ExperimentalApi.getApi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener, LinkListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();

    private static final int DEFAULT_UDP_PORT = 14550;
    private static final int DEFAULT_USB_BAUD_RATE = 57600;

    private Spinner modeSelector;

    private Button startVideoStream;
    private Button stopVideoStream;

    private Button startVideoStreamUsingObserver;
    private Button stopVideoStreamUsingObserver;

    private MediaCodecManager mediaCodecManager;

    private TextureView videoView;

    private String videoTag = "testvideotag";

    Handler mainHandler;

    // add 3d
    MyGLView glView;
    private GestureDetector gesDetector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        final Button takePic = (Button) findViewById(R.id.take_photo_button);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        final Button toggleVideo = (Button) findViewById(R.id.toggle_video_recording);
        toggleVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVideoRecording();
            }
        });

        videoView = (TextureView) findViewById(R.id.video_content);
        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                alertUser("Video display is available.");
                startVideoStream.setEnabled(true);
                startVideoStreamUsingObserver.setEnabled(true);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                startVideoStream.setEnabled(false);
                startVideoStreamUsingObserver.setEnabled(false);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        startVideoStream = (Button) findViewById(R.id.start_video_stream);
        startVideoStream.setEnabled(false);
        startVideoStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Starting video stream.");
                startVideoStream(new Surface(videoView.getSurfaceTexture()));
            }
        });

        stopVideoStream = (Button) findViewById(R.id.stop_video_stream);
        stopVideoStream.setEnabled(false);
        stopVideoStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Stopping video stream.");
                stopVideoStream();
            }
        });

        startVideoStreamUsingObserver = (Button) findViewById(R.id.start_video_stream_using_observer);
        startVideoStreamUsingObserver.setEnabled(false);
        startVideoStreamUsingObserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Starting video stream using observer for video stream packets.");
                startVideoStreamForObserver();
            }
        });

        stopVideoStreamUsingObserver = (Button) findViewById(R.id.stop_video_stream_using_observer);
        stopVideoStreamUsingObserver.setEnabled(false);
        stopVideoStreamUsingObserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertUser("Stopping video stream using observer for video stream packets.");
                stopVideoStreamForObserver();
            }
        });

        // Initialize media codec manager to decode video stream packets.
        HandlerThread mediaCodecHandlerThread = new HandlerThread("MediaCodecHandlerThread");
        mediaCodecHandlerThread.start();
        Handler mediaCodecHandler = new Handler(mediaCodecHandlerThread.getLooper());
        mediaCodecManager = new MediaCodecManager(mediaCodecHandler);

        mainHandler = new Handler(getApplicationContext().getMainLooper());

        // add 3d
        glView = (MyGLView)findViewById(R.id.surfaceView);
//        glView.setEGLConfigChooser(8, 8, 8, 8, 8, 8);
//        glView.setRenderer(new MyRenderer());
        gesDetector = new GestureDetector(this, glView);
        final Button clearGLViewButton = (Button) findViewById(R.id.clearGLView);
        clearGLViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearGLView();
            }
        });

        // add manual
        ToggleButton toggleManual = (ToggleButton)findViewById(R.id.toggleManual);
        toggleManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ControlApi.getApi(drone).enableManualControl(isChecked, new ControlApi.ManualControlStateListener() {
                    @Override
                    public void onManualControlToggled(boolean isEnabled) {
                        if (isEnabled) {
                            alertUser("Enabled: Manual.");
                        } else {
                            alertUser("disabled: Manual.");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
        updateVehicleModesForType(this.droneType);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
            updateConnectedButton(false);
        }

        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }

    // add 3d
    @Override
    protected void onResume(){
        super.onResume();
        glView.onResume();
    }

    // add 3d
    @Override
    protected void onPause(){
        super.onPause();
        glView.onPause();
    }

    // add 3d
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gesDetector.onTouchEvent(event);
    }

    // add 3d
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("DispatchTouchEvent","call");
        gesDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    // add takeoff
    public void onGetTakeoff(View view){
        EditText takeoff = (EditText)findViewById(R.id.takeoffEditText);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        Home droneHome = this.drone.getAttribute(AttributeType.HOME);
        takeoff.setText(String.format("%3.1f", droneAltitude.getAltitude() - droneHome.getCoordinate().getAltitude()));
    }
    public void onSetTakeoff(View view) {
        EditText takeoff = (EditText)findViewById(R.id.takeoffEditText);
        if (!takeoff.getText().toString().isEmpty()) {
            try {
                double altitude = Double.parseDouble(takeoff.getText().toString());
                // Take off
                Log.d("ControlApiTest", "takeoff(" + altitude + ")");
                ControlApi.getApi(this.drone).takeoff(altitude, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        alertUser("Success: Takeoff.");
                    }
                    @Override
                    public void onError(int i) {
                        alertUser("Error: Takeoff.");
                    }
                    @Override
                    public void onTimeout() {
                        alertUser("Timeout: Takeoff.");
                    }
                });
            } catch (Exception e) {
                alertUser("Exception: Takeoff.");
            }
        }
    }

    // add pause
    public void onSetPause(View view) {
        Log.d("ControlApiTest", "pauseAtCurrentLocation()");
        ControlApi.getApi(this.drone).pauseAtCurrentLocation(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Success: Pause.");
            }
            @Override
            public void onError(int i) {
                alertUser("Error: Pause.");
            }
            @Override
            public void onTimeout() {
                alertUser("Timeout: Pause.");
            }
        });
    }

    // add go to
    public void onGetGoTo(View view){
        EditText textLat = (EditText)findViewById(R.id.goToEditTextLat);
        EditText textLong = (EditText)findViewById(R.id.goToEditTextLong);
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        if (droneGps.isValid()) {
            LatLong vehiclePosition = droneGps.getPosition();
            textLat.setText(String.format("%.7f", vehiclePosition.getLatitude()));
            textLong.setText(String.format("%.7f", vehiclePosition.getLongitude()));
        } else {
            alertUser("GPS is invalid: Go to.");
        }
    }
    public void onSetGoTo(View view) {
        EditText textLat = (EditText)findViewById(R.id.goToEditTextLat);
        EditText textLong = (EditText)findViewById(R.id.goToEditTextLong);
        if (!textLat.getText().toString().isEmpty() && !textLong.getText().toString().isEmpty()) {
            try {
                double latitude = Double.parseDouble(textLat.getText().toString());
                double longitude = Double.parseDouble(textLong.getText().toString());
                // Go To
                LatLong latLong = new LatLong(latitude, longitude);
                Log.d("ControlApiTest", "goTo(" + latLong + ", true)");
                ControlApi.getApi(this.drone).goTo(latLong, true, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        alertUser("Success: Go to.");
                    }
                    @Override
                    public void onError(int i) {
                        alertUser("Error: Go to.");
                    }
                    @Override
                    public void onTimeout() {
                        alertUser("Timeout: Go to.");
                    }
                });
            } catch (Exception e) {
                alertUser("Exception: Go to.");
            }
        }
    }

    // add look at
    public void onGetLookAt(View view){
        EditText textLat = (EditText)findViewById(R.id.lookAtEditTextLat);
        EditText textLong = (EditText)findViewById(R.id.lookAtEditTextLong);
        EditText textAlt = (EditText)findViewById(R.id.lookAtEditTextAlt);
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        if (droneGps.isValid()) {
            LatLong vehiclePosition = droneGps.getPosition();
            Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
            textLat.setText(String.format("%.7f", vehiclePosition.getLatitude()));
            textLong.setText(String.format("%.7f", vehiclePosition.getLongitude()));
            textAlt.setText(String.format("%3.1f", droneAltitude.getAltitude()));
        } else {
            alertUser("GPS is invalid: Look at.");
        }
    }
    public void onSetLookAt(View view) {
        EditText textLat = (EditText)findViewById(R.id.lookAtEditTextLat);
        EditText textLong = (EditText)findViewById(R.id.lookAtEditTextLong);
        EditText textAlt = (EditText)findViewById(R.id.lookAtEditTextAlt);
        if (!textLat.getText().toString().isEmpty() && !textLong.getText().toString().isEmpty() && !textAlt.getText().toString().isEmpty()) {
            try {
                double latitude = Double.parseDouble(textLat.getText().toString());
                double longitude = Double.parseDouble(textLong.getText().toString());
                double altitude = Double.parseDouble(textAlt.getText().toString());
                // Look at
                LatLongAlt latLongAlt = new LatLongAlt(latitude, longitude, altitude);
                Log.d("ControlApiTest", "lookAt(" + latLongAlt + ", true)");
                ControlApi.getApi(this.drone).lookAt(latLongAlt, true, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        alertUser("Success: Look at.");
                    }
                    @Override
                    public void onError(int i) {
                        alertUser("Error: Look at.");
                    }
                    @Override
                    public void onTimeout() {
                        alertUser("Timeout: Look at.");
                    }
                });
            } catch (Exception e) {
                alertUser("Exception: Look at.");
            }
        }
    }

    // add climb to
    public void onGetClimbTo(View view){
        EditText climbTo = (EditText)findViewById(R.id.climbToEditText);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        Home droneHome = this.drone.getAttribute(AttributeType.HOME);
        climbTo.setText(String.format("%3.1f", droneAltitude.getAltitude() - droneHome.getCoordinate().getAltitude()));
    }
    public void onSetClimbTo(View view) {
        EditText climbTo = (EditText)findViewById(R.id.climbToEditText);
        if (!climbTo.getText().toString().isEmpty()) {
            try {
                double altitude = Double.parseDouble(climbTo.getText().toString());
                // Climb to
                Log.d("ControlApiTest", "climbTo(" + altitude + ")");
                ControlApi.getApi(this.drone).climbTo(altitude);
                alertUser("Success: Climb to.");
            } catch (Exception e) {
                alertUser("Exception: Climb to.");
            }
        }
    }

    // add turn to
    public void onSetTurnTo(View view) {
        EditText angleText = (EditText)findViewById(R.id.turnToEditTextAngle);
        EditText rateText = (EditText)findViewById(R.id.turnToEditTextRate);
        if (!angleText.getText().toString().isEmpty() && !rateText.getText().toString().isEmpty()) {
            try {
                float angle = Float.parseFloat(angleText.getText().toString());
                float rate = Float.parseFloat(rateText.getText().toString());
                // Turn to
                Log.d("ControlApiTest", "turnTo(" + angle + ", " + rate + ", false)");
                ControlApi.getApi(this.drone).turnTo(angle, rate, false, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        alertUser("Success: Turn to.");
                    }
                    @Override
                    public void onError(int i) {
                        alertUser("Error: Turn to.");
                    }
                    @Override
                    public void onTimeout() {
                        alertUser("Timeout: Turn to.");
                    }
                });
            } catch (Exception e) {
                alertUser("Exception: Turn to.");
            }
        }
    }

    // add manual
    public void onSetManual(View view) {
        EditText textX = (EditText)findViewById(R.id.manualEditTextX);
        EditText textY = (EditText)findViewById(R.id.manualEditTextY);
        EditText textZ = (EditText)findViewById(R.id.manualEditTextZ);
        if (!textX.getText().toString().isEmpty() && !textY.getText().toString().isEmpty() && !textZ.getText().toString().isEmpty()) {
            try {
                float x = Float.parseFloat(textX.getText().toString());
                float y = Float.parseFloat(textY.getText().toString());
                float z = Float.parseFloat(textZ.getText().toString());
                // Manual
                Log.d("ControlApiTest", "manualControl(" + x + ", " + y + ", " + z + ")");
                ControlApi.getApi(this.drone).manualControl(x, y, z, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        alertUser("Success: Manual.");
                    }
                    @Override
                    public void onError(int i) {
                        alertUser("Error: Manual.");
                    }
                    @Override
                    public void onTimeout() {
                        alertUser("Timeout: Manual.");
                    }
                });
            } catch (Exception e) {
                alertUser("Exception: Manual.");
            }
        }
    }

    // DroneKit-Android Listener
    // ==========================================================

    @Override
    public void onTowerConnected() {
        alertUser("DroneKit-Android Connected");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {
        alertUser("DroneKit-Android Interrupted");
    }

    // Drone Listener
    // ==========================================================

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
                updateConnectedButton(this.drone.isConnected());
                updateArmButton();
                checkSoloState();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
                updateConnectedButton(this.drone.isConnected());
                updateArmButton();
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateArmButton();
                break;

            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                updateVehicleMode();
                break;

            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;

            case AttributeEvent.HOME_UPDATED:
                updateDistanceFromHome();
                break;

            default:
                // Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
                break;
        }
    }

    private void checkSoloState() {
        final SoloState soloState = drone.getAttribute(SoloAttributes.SOLO_STATE);
        if (soloState == null){
            alertUser("Unable to retrieve the solo state.");
        }
        else {
            alertUser("Solo state is up to date.");
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    // UI Events
    // ==========================================================

    public void onBtnConnectTap(View view) {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            Spinner connectionSelector = (Spinner) findViewById(R.id.selectConnectionType);
            int selectedConnectionType = connectionSelector.getSelectedItemPosition();

            ConnectionParameter connectionParams = selectedConnectionType == ConnectionType.TYPE_USB
                ? ConnectionParameter.newUsbConnection(null)
                : ConnectionParameter.newUdpConnection(null);

            this.drone.connect(connectionParams);
        }

    }

    public void onFlightModeSelected(View view) {
        VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();

        VehicleApi.getApi(this.drone).setVehicleMode(vehicleMode, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Vehicle mode change successful.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Vehicle mode change failed: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Vehicle mode change timed out.");
            }
        });
    }

    public void onArmButtonTap(View view) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        if (vehicleState.isFlying()) {
            // Land
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LAND, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUser("Unable to land the vehicle.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Unable to land the vehicle.");
                }
            });
        } else if (vehicleState.isArmed()) {
            // Take off
            ControlApi.getApi(this.drone).takeoff(10, new AbstractCommandListener() {

                @Override
                public void onSuccess() {
                    alertUser("Taking off...");
                }

                @Override
                public void onError(int i) {
                    alertUser("Unable to take off.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Unable to take off.");
                }
            });
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("Connect to a drone first");
        } else {
            // Connected but not Armed
            VehicleApi.getApi(this.drone).arm(true, false, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUser("Unable to arm vehicle.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Arming operation timed out.");
                }
            });
        }
    }

    // UI updating
    // ==========================================================

    protected void updateConnectedButton(Boolean isConnected) {
        Button connectButton = (Button) findViewById(R.id.btnConnect);
        if (isConnected) {
            connectButton.setText("Disconnect");
        } else {
            connectButton.setText("Connect");
        }
    }

    protected void updateArmButton() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button) findViewById(R.id.btnArmTakeOff);

        if (!this.drone.isConnected()) {
            armButton.setVisibility(View.INVISIBLE);
        } else {
            armButton.setVisibility(View.VISIBLE);
        }

        if (vehicleState.isFlying()) {
            // Land
            armButton.setText("LAND");
        } else if (vehicleState.isArmed()) {
            // Take off
            armButton.setText("TAKE OFF");
        } else if (vehicleState.isConnected()) {
            // Connected but not Armed
            armButton.setText("ARM");
        }
    }

    protected void updateAltitude() {
        TextView altitudeTextView = (TextView) findViewById(R.id.altitudeValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
        // add 3d
        double vehicleAltitude = droneAltitude.getAltitude();
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();
        if (droneGps.isValid()) {
            glView.setPositions(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
        } else {
            Log.w(TAG, "GPS is invalid.");
        }
    }

    protected void updateSpeed() {
        TextView speedTextView = (TextView) findViewById(R.id.speedValueTextView);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }

    protected void updateDistanceFromHome() {
        TextView distanceTextView = (TextView) findViewById(R.id.distanceValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        double vehicleAltitude = droneAltitude.getAltitude();
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();

        double distanceFromHome = 0;

        if (droneGps.isValid()) {
            LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
            Home droneHome = this.drone.getAttribute(AttributeType.HOME);
            distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
        } else {
            distanceFromHome = 0;
        }

        distanceTextView.setText(String.format("%3.1f", distanceFromHome) + "m");
    }

    protected void updateVehicleModesForType(int droneType) {

        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
        vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modeSelector.setAdapter(vehicleModeArrayAdapter);
    }

    protected void updateVehicleMode() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        ArrayAdapter arrayAdapter = (ArrayAdapter) this.modeSelector.getAdapter();
        this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
    }

    // Helper methods
    // ==========================================================

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }

    private void runOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
        if (pointA == null || pointB == null) {
            return 0;
        }
        double dx = pointA.getLatitude() - pointB.getLatitude();
        double dy = pointA.getLongitude() - pointB.getLongitude();
        double dz = pointA.getAltitude() - pointB.getAltitude();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // add 3d
    private void clearGLView() {
        glView.clearGLView();
    }

    private void takePhoto() {
        SoloCameraApi.getApi(drone).takePhoto(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Photo taken.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error while trying to take the photo: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timeout while trying to take the photo.");
            }
        });
    }

    private void toggleVideoRecording() {
        SoloCameraApi.getApi(drone).toggleVideoRecording(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Video recording toggled.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error while trying to toggle video recording: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timeout while trying to toggle video recording.");
            }
        });
    }

    private void startVideoStream(Surface videoSurface) {
        SoloCameraApi.getApi(drone).startVideoStream(videoSurface, videoTag, true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Successfully started the video stream. ");

                if (stopVideoStream != null)
                    stopVideoStream.setEnabled(true);

                if (startVideoStream != null)
                    startVideoStream.setEnabled(false);

                if (startVideoStreamUsingObserver != null)
                    startVideoStreamUsingObserver.setEnabled(false);

                if (stopVideoStreamUsingObserver != null)
                    stopVideoStreamUsingObserver.setEnabled(false);
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error while starting the video stream: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timed out while attempting to start the video stream.");
            }
        });
    }

    DecoderListener decoderListener = new DecoderListener() {
        @Override
        public void onDecodingStarted() {
            alertUser("MediaCodecManager: video decoding started...");
        }

        @Override
        public void onDecodingError() {
            alertUser("MediaCodecManager: video decoding error...");
        }

        @Override
        public void onDecodingEnded() {
            alertUser("MediaCodecManager: video decoding ended...");
        }
    };

    private void startVideoStreamForObserver() {
        getApi(drone).startVideoStream(videoTag, new ExperimentalApi.IVideoStreamCallback() {
            @Override
            public void onVideoStreamConnecting() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        alertUser("Successfully obtained lock for drone video stream.");
                    }
                });
            }

            @Override
            public void onVideoStreamConnected() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        alertUser("Successfully opened drone video connection.");

                        if (stopVideoStreamUsingObserver != null)
                            stopVideoStreamUsingObserver.setEnabled(true);

                        if (startVideoStreamUsingObserver != null)
                            startVideoStreamUsingObserver.setEnabled(false);

                        if (stopVideoStream != null)
                            stopVideoStream.setEnabled(false);

                        if (startVideoStream != null)
                            startVideoStream.setEnabled(false);
                    }
                });

                mediaCodecManager.stopDecoding(new DecoderListener() {
                    @Override
                    public void onDecodingStarted() {
                    }

                    @Override
                    public void onDecodingError() {
                    }

                    @Override
                    public void onDecodingEnded() {
                        try {
                            mediaCodecManager.startDecoding(new Surface(videoView.getSurfaceTexture()),
                                decoderListener);
                        } catch (IOException | IllegalStateException e) {
                            Log.e(TAG, "Unable to create media codec.", e);
                            if (decoderListener != null)
                                decoderListener.onDecodingError();
                        }
                    }
                });
            }

            @Override
            public void onVideoStreamDisconnecting() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        alertUser("Successfully released lock for drone video stream.");
                    }
                });
            }

            @Override
            public void onVideoStreamDisconnected() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        alertUser("Successfully closed drone video connection.");

                        if (stopVideoStreamUsingObserver != null)
                            stopVideoStreamUsingObserver.setEnabled(false);

                        if (startVideoStreamUsingObserver != null)
                            startVideoStreamUsingObserver.setEnabled(true);

                        if (stopVideoStream != null)
                            stopVideoStream.setEnabled(false);

                        if (startVideoStream != null)
                            startVideoStream.setEnabled(true);
                    }
                });

                mediaCodecManager.stopDecoding(decoderListener);
            }

            @Override
            public void onError(int executionError) {
                alertUser("Error while getting lock to vehicle video stream: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Timed out while attempting to get lock for vehicle video stream.");
            }

            @Override
            public void onAsyncVideoStreamPacketReceived(byte[] data, int dataSize) {
                mediaCodecManager.onInputDataReceived(data, dataSize);
            }
        });
    }

    private void stopVideoStream() {
        SoloCameraApi.getApi(drone).stopVideoStream(videoTag, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                if (stopVideoStream != null)
                    stopVideoStream.setEnabled(false);

                if (startVideoStream != null)
                    startVideoStream.setEnabled(true);

                if (stopVideoStreamUsingObserver != null)
                    stopVideoStreamUsingObserver.setEnabled(false);

                if (startVideoStreamUsingObserver != null)
                    startVideoStreamUsingObserver.setEnabled(true);
            }

            @Override
            public void onError(int executionError) {
            }

            @Override
            public void onTimeout() {
            }
        });
    }

    private void stopVideoStreamForObserver() {
        getApi(drone).stopVideoStream(videoTag);
    }

    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        switch(connectionStatus.getStatusCode()){
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String msg = null;
                if (extras != null) {
                    msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }
                alertUser("Connection Failed:" + msg);
                break;
        }
    }
}
