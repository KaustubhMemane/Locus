package com.kmema.android.locus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private ActivityDetecttionBroadCastReceiver mBroadCastReceiver;
    private final String LOG_TAG = "KaustubMemaneTestingApp";
    private TextView mtextView;
    private TextView mTextViewLatitude;
    private TextView mtextViewLongitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private TextView mStatusText;
    private Button RequestUpdateButton;
    private Button RemoveUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        RequestUpdateButton = (Button) findViewById(R.id.request_activity_updates_button);
        RemoveUpdateButton = (Button) findViewById(R.id.remove_activity_updates_button);
        mStatusText = (TextView) findViewById(R.id.status_text);

        mtextView = (TextView) findViewById(R.id.txtoutput);
        mTextViewLatitude = (TextView) findViewById(R.id.textViewLatitude);
        mtextViewLongitude = (TextView) findViewById(R.id.textviewLongitude);
        mBroadCastReceiver = new ActivityDetecttionBroadCastReceiver();
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            mTextViewLatitude.setText(String.valueOf(mLastLocation.getLatitude()));
            mtextViewLongitude.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void requestActivityUpdatesButtonHandler(View view)
    {
        if(!mGoogleApiClient.isConnected())
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
        requs
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i(LOG_TAG,location.toString());
        mtextView.setText(location.toString());
    }

    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }


    public class ActivityDetecttionBroadCastReceiver extends BroadcastReceiver
    {
        private final static String TAG = "Receiver";
        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

            String strStatus = "";
            for(DetectedActivity thisActivity: updatedActivities){
                strStatus +=  getActivityString(thisActivity.getType()) + thisActivity.getConfidence() + "%\n";
            }
            mStatusText.setText(strStatus);
        }

    }
}
