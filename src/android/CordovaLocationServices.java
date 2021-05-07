/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package com.cardcam.scantrans;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.cardcam.car_num_rcgn_lib;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
//import org.apache.cordova.camera.CameraLauncher;
import org.apache.cordova.camera.CameraOptions;
//import org.apache.cordova.camera.CordovaUri;
//import org.apache.cordova.camera.ExifHelper;
import org.apache.cordova.camera.GApiUtils;
import org.apache.cordova.camera.LocationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class CordovaLocationServices implements
        GoogleApiClient.ConnectionCallbacks {

    private static final int LOCATION_PERMISSION_REQUEST = 0;

    private CordovaLocationListener mListener;
    private boolean mWantLastLocation = false;
    private boolean mWantUpdates = false;
    private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private JSONArray mPrevArgs;
    private CallbackContext mCbContext;
    private GApiUtils mGApiUtils;
    private GoogleApiClient mGApiClient;
    public CordovaInterface cordova;


    public String sourcePath;
    public int rotate;
    public String thisJson;
    public ExifHelper exif;
    private CordovaUri imageUri;
    public Intent intent;
    public CameraOptions cameraOptions;
    CallbackContext callbackContext;


    public CordovaLocationServices(CordovaInterface cordova) {
        this.cordova = cordova;
        mGApiClient = new GoogleApiClient.Builder(cordova.getActivity())
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(getGApiUtils()).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LocationUtils.APPTAG, "Location Services connected");
        if (mWantLastLocation) {
            mWantLastLocation = false;
            getLastLocation();
        }
        if (mListener != null && mWantUpdates) {
            mWantUpdates = false;
            mListener.start();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LocationUtils.APPTAG, "GoogleApiClient connection has been suspend");
    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid, or false if not.
     */
    public boolean getLocation(final CallbackContext callbackContext,
                               String sourcePath,
                               int rotate,
                               String thisJson,
                               ExifHelper exif,
                               CordovaUri imageUri,
                               Intent intent,
                               CameraOptions cameraOptions
    ) {

        this.sourcePath = sourcePath;
        this.rotate = rotate;
        this.thisJson = thisJson;
        this.exif = exif;
        this.imageUri = imageUri;
        this.intent = intent;
        this.callbackContext = callbackContext;
        this.cameraOptions = cameraOptions;

        final String id = "";
        final boolean highAccuracy = true;
        final int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        final long interval = LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS;
        final long fastInterval = LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS;

        if (highAccuracy && isGPSdisabled()) {
            try {
                car_num_rcgn_lib cameraLauncher = new car_num_rcgn_lib(this.cordova, callbackContext, this.imageUri);
                cameraLauncher.processPhotoFromCamera(this.sourcePath, this.rotate, this.thisJson, this.exif, this.intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (getGApiUtils().servicesConnected()) {
                if (!mGApiClient.isConnected() && !mGApiClient.isConnecting()) {
                    mGApiClient.connect();
                }

                if (mGApiClient.isConnected()) {
                    getLastLocation(callbackContext);
                } else {
                    setWantLastLocation(callbackContext);
                }

            }
        }

        return true;
    }


    /**
     * Called when the activity is to be shut down. Stop listener.
     */
    public void onDestroy() {
        if (mListener != null) {
            mListener.destroy();
        }
        if (mGApiClient.isConnected() || mGApiClient.isConnecting()) {
            // After disconnect() is called, the client is considered "dead".
            mGApiClient.disconnect();
        }
    }

    /**
     * Called when the view navigates. Stop the listeners.
     */
    public void onReset() {
        this.onDestroy();
    }

    public JSONObject returnLocationJSON(Location loc, CallbackContext callbackContext) {
        JSONObject o = new JSONObject();

        try {
            o.put("latitude", loc.getLatitude());
            o.put("longitude", loc.getLongitude());
            o.put("altitude", (loc.hasAltitude() ? loc.getAltitude() : null));
            o.put("accuracy", loc.getAccuracy());
            o.put("heading", (loc.hasBearing() ? (loc.hasSpeed() ? loc.getBearing() : null) : null));
            o.put("velocity", loc.getSpeed());
            o.put("timestamp", loc.getTime());

            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();

            Gson g = new Gson();

            ExifHelper exifHelper = g.fromJson(this.thisJson, ExifHelper.class);
            exifHelper.setGpsLatitudeRef(latitude >= 0 ? "N" : "S");
            exifHelper.setGpsLatitude(getDms(latitude));
            exifHelper.setGpsLongitudeRef(longitude >= 0 ? "E" : "W");
            exifHelper.setGpsLongitude(getDms(longitude));
            car_num_rcgn_lib cameraLauncher = new car_num_rcgn_lib(this.cordova, callbackContext, this.imageUri);
            cameraLauncher.processPhotoFromCamera(this.sourcePath, this.rotate, g.toJson(exifHelper), exifHelper, this.intent);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }


    public void savePhotoFromCamera() {
        try {
            car_num_rcgn_lib cameraLauncher = new car_num_rcgn_lib(this.cordova, this.callbackContext, this.imageUri);
            cameraLauncher.processPhotoFromCamera(this.sourcePath, this.rotate, this.thisJson, this.exif, this.intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JSONObject win(Location loc, CallbackContext callbackContext, boolean keepCallback) {
        return this.returnLocationJSON(loc, callbackContext);
    }

    /**
     * Location failed. Send error back to JavaScript.
     *
     * @param code The error code
     * @param msg  The error message
     */
    public void fail(int code, String msg, CallbackContext callbackContext, boolean keepCallback) {
        this.savePhotoFromCamera();
    }

    private boolean isGPSdisabled() {
        boolean gps_enabled;
        LocationManager lm = (LocationManager) this.cordova.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            gps_enabled = false;
        }

        return !gps_enabled;
    }

    private void getLastLocation() {
        getLastLocation(mCbContext);
        mCbContext = null;
        mPrevArgs = null;
    }

    private void getLastLocation(CallbackContext callbackContext) {
        int maximumAge = 100000;

        try {
            Location last = null;
            if (mGApiClient != null && mGApiClient.isConnected()) {
                last = LocationServices.FusedLocationApi.getLastLocation(mGApiClient);
            }
//            // Check if we can use lastKnownLocation to get a quick reading and use
//            // less battery

            if (last != null && (System.currentTimeMillis() - last.getTime()) <= maximumAge) {
                returnLocationJSON(last, callbackContext);
            } else {
                getCurrentLocation(callbackContext, 5000);
            }

        } catch (Exception e) {
            getCurrentLocation(callbackContext, 5000);
            e.printStackTrace();
        }

    }


    public String getDms(double val) {
        //return "" + val;
        double valDeg;
        double valMin;
        double valSec;
        String result;

        val = Math.abs(val);

        valDeg = Math.floor(val);
        result = String.valueOf((int) valDeg) + "/1,";

        valMin = Math.floor((val - valDeg) * 60);
        result += String.valueOf((int) valMin) + "/1,";

        valSec = Math.round((val - valDeg - valMin / 60) * 3600 * 10000);
        result += String.valueOf((int) valSec) + "/10000";

        return result;
    }


    private void setWantLastLocation(CallbackContext callbackContext) {
        mCbContext = callbackContext;
        mWantLastLocation = true;
    }

    private void clearWatch(String id) {
        getListener().clearWatch(id);
    }

    private void getCurrentLocation(CallbackContext callbackContext, int timeout) {
        getListener().addCallback(callbackContext, timeout);
    }

    private void addWatch(String timerId, CallbackContext callbackContext) {
        getListener().addWatch(timerId, callbackContext);
    }

    private CordovaLocationListener getListener() {
        if (mListener == null) {
            mListener = new CordovaLocationListener(mGApiClient, this,
                    LocationUtils.APPTAG);
        }
        return mListener;
    }

    private GApiUtils getGApiUtils() {
        if (mGApiUtils == null) {
            mGApiUtils = new GApiUtils(cordova);
        }
        return mGApiUtils;
    }
}
