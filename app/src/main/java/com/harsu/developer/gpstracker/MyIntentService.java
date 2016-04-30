package com.harsu.developer.gpstracker;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import app.AppController;
import app.ControllerConstants;
import app.VolleySingleton;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MyIntentService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String uniqueID;
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            sendRequest(location);
            Toast.makeText(MyIntentService.this, mLastLocation.getLatitude() + " " + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    };
    LocationRequest mLocationRequest;

    private void sendRequest(final Location location) {
        try {
            String url = "?uid=" + URLEncoder.encode(uniqueID, "UTF-8") + "&latitude=" + URLEncoder.encode(location.getLatitude() + "", "UTF-8")
                    + "&longitude=" + URLEncoder.encode(location.getLongitude() + "", "UTF-8") + "&time=" + URLEncoder.encode(location.getTime()/1000 + "", "UTF-8") ;
            url = ControllerConstants.url + url;

//        try {

//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Log.e("GPS response", s);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("GPS error", volleyError.toString());
                }
            }) /*{
            @Override
            public byte[] getBody() throws AuthFailureError {

                JSONObject object=new JSONObject();
                try {
                    object.put("uid",uniqueID);
                    object.put("latitude",location.getLatitude());
                    object.put("longitude",location.getLongitude());
                    object.put("time",location.getTime());
                    Log.e("body",object.toString());
                    return object.toString().getBytes();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "".getBytes();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uniqueID);
                params.put("latitude", location.getLatitude() + "");
                params.put("longitude", location.getLongitude() + "");
                params.put("time", location.getTime() + "");

                return params;

            }
        }*/;

            VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
        } catch (UnsupportedEncodingException e) {

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        SharedPreferences preferences = getSharedPreferences("Id", MODE_PRIVATE);
        uniqueID = preferences.getString("uid", "");
        if (uniqueID.isEmpty()) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uid", uniqueID);
            editor.commit();
        }
        Log.e("uniqueId", uniqueID);
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(MyIntentService.this, "Started Service", Toast.LENGTH_SHORT).show();

        return Service.START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastLocation();
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Toast.makeText(MyIntentService.this, "Service destroyed", Toast.LENGTH_SHORT).show();
    }

    private void getLastLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(this, mLastLocation.getLatitude() + " " + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
        }

    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60 * 1000);
        mLocationRequest.setFastestInterval(10 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
