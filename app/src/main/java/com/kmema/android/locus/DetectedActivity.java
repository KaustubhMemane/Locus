package com.kmema.android.locus;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;

import java.util.ArrayList;

/**
 * Created by kmema on 6/14/2017.
 */

public class DetectedActivity extends IntentService{

    private static final String TAG = "detection_intent_servic";

    public DetectedActivity()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent LocalIntent = new Intent(Constants.BROADCAST_ACTION);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();


        Log.i(TAG,"Activities Detected/found");

        LocalIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(LocalIntent);
    }
}
