package com.emperor.stayhomechallenge.monitor.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.emperor.stayhomechallenge.Constants;
import com.emperor.stayhomechallenge.R;
import com.emperor.stayhomechallenge.monitor.activities.MainActivity;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.emperor.stayhomechallenge.Constants.GPS_SERVICE_VALUE;


public class BGServicenormal extends Service {
    public static final String APIKEY = "AIzaSyB9kdMz4eGNnXSMSMQ0cGLG7tHq6bNLr18";
    private static final String TAG = "BGServicenormal";
    private static final int TWENTY_MINUTES = 0* 60 * 1000;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private int seconds = 0;
    // Is the stopwatch running?
    private boolean running = false;
    private Handler handler;
    private Runnable timerRunnable;
    private Location defaultLocation;
    private String homeAddress = null;
    private double dist;



    private boolean isConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        return isConnected;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            startForeground(12345678, startNotification());
            getLocationDetails();
            running = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Notification startNotification()
    {
        Notification notification=null;
        try {
            Log.d(TAG, "startServiceWithNotification: first");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.putExtra(Constants.INTENT_CODE,"service");
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            Log.d(TAG, "startServiceWithNotification: second");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setSmallIcon(R.drawable.icon_home)
                    .setContentText("")
//                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true);

            Log.d(TAG, "startServiceWithNotification: third");
            notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;// NO_CLEAR makes the notification stay when the user performs a "delete all" command
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Monitoring your Location");
                builder.setChannelId(NOTIFICATION_CHANNEL_ID);
                Log.d(TAG, "startServiceWithNotification: four");
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(channel);
            }

            Log.d(TAG, "startServiceWithNotification: five");
//            startForeground(NOTIFICATION_ID, notification);
//            mNotificationManager.notify(0 /* Request Code */, builder.build());
//            startForeground(1, notification);

            Log.d(TAG, "startServiceWithNotification: six");
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "startServiceWithNotification: error");
        }
        return notification;
    }

    Notification startPushNotification()
    {
        Notification notification=null;
        try {
            Log.d(TAG, "startServiceWithNotification: first");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.putExtra(Constants.GPS_OFF_KEY,GPS_SERVICE_VALUE);
            notificationIntent.putExtra(Constants.TIMER_KEY,getTime());
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d(TAG, "startServiceWithNotification: second");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setSmallIcon(R.drawable.icon_home)
                    .setContentText("You have switched Off your GPS")
//                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(false)
                    .setAutoCancel(true);;

            Log.d(TAG, "startServiceWithNotification: third");
            notification = builder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;// NO_CLEAR makes the notification stay when the user performs a "delete all" command
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Monitoring your Location");
                builder.setChannelId(NOTIFICATION_CHANNEL_ID);
                Log.d(TAG, "startServiceWithNotification: four");
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(channel);
            }

            Log.d(TAG, "startServiceWithNotification: five");
//            startForeground(NOTIFICATION_ID, notification);
            mNotificationManager.notify(0 /* Request Code */, builder.build());
//            startForeground(1, notification);

            Log.d(TAG, "startServiceWithNotification: six");
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "startServiceWithNotification: error");
        }
        return notification;
    }

    public void getLocationDetails() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TWENTY_MINUTES, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TWENTY_MINUTES, 0, listener);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        handler.removeCallbacks(timerRunnable);
        homeAddress = null;
        defaultLocation = null;
        seconds = 0;
        Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY, false);
        System.gc();

    }

    private void runTimer()
    {
        // Creates a new Handler
        handler = new Handler();
        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        timerRunnable = new Runnable() {
            @Override
            public void run() {

                if(checkGpsEnabled()){
                    // Format the seconds into hours, minutes,
                    // and seconds.
                    String time = getTime();
                    // Set the text view text.
//                timeView.setText(time);
                    // If running is true, increment the
                    // seconds variable.
                    if (running) {
                        seconds++;
                        sendBroadcastMessage(time,"");
                    }
                    handler.postDelayed(this, 1000);
                }

                else {
                    startPushNotification();
                    stopMyService();
                    sendBroadcastMessage(getTime(),Constants.GPS_OFF_KEY);
                }


                // Post the code again
                // with a delay of 1 second.
            }
        };
        handler.post(timerRunnable);
    }

    private String getTime()
    {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return  String
            .format(Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs);
    }

    private boolean checkGpsEnabled()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    @Override
    public void onTaskRemoved(final Intent rootIntent) {
//        startActivity(getPopupIntent());

    }


    void stopMyService() {
        stopForeground(true);
        stopSelf();
    }

    private boolean checkDeviceIsMoved(Location current, Location last) {
        try {
            dist = current.distanceTo(last) / 1000;
//            Log.v("distance Calculate ", Double.toString(dist));

            NumberFormat df = DecimalFormat.getInstance();
            df.setMaximumFractionDigits(3);
            df.setGroupingUsed(false);

            String distanc = df.format(dist).toString().replaceAll(",", ".");
            dist = Double.parseDouble(distanc);

            //  dist = Double.parseDouble(df.format(dist));

//            Log.v("dist Calculate Decimal ", Double.toString(dist));
            if (dist <= 0.300) { //(in km, you can use 0.1 for metres etc.)
                //If it's within 1km, we assume we're not moving
//                Log.v("Device is = ", "Not  Moved");
//                return false;
                return true;
            } else {
//                Log.v("Device is = ", "Moved");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");

        runTimer();

        return START_REDELIVER_INTENT ;
    }

    private void sendBroadcastMessage(String time, String address) {
        if (time != null) {
            Intent intent = new Intent(Constants.TIMER_INTENT);
            intent.putExtra(Constants.TIMER_KEY, time);
            intent.putExtra(Constants.CURRENT_ADDRESS_KEY, address);
            intent.putExtra(Constants.HOME_ADDRESS_KEY, homeAddress);
            intent.putExtra(Constants.DISTANCE_KEY, dist);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private class GeocodeAsyncTask extends AsyncTask<Double, Void, Address> {

        String errorMessage = "";

        @Override
        protected Address doInBackground(Double... latlang) {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            if (geocoder.isPresent()) {
                try {
                    addresses = geocoder.getFromLocation(latlang[0], latlang[1], 1);
//                    Log.d(TAG, "doInBackground: ************");
                } catch (IOException ioException) {
                    errorMessage = "Service Not Available";
//                    Log.e(TAG, errorMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    errorMessage = "Invalid Latitude or Longitude Used";
//                    Log.e(TAG, errorMessage + ". " +
//                            "Latitude = " + latlang[0] + ", Longitude = " +
//                            latlang[1], illegalArgumentException);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (addresses != null && addresses.size() > 0)
                    return addresses.get(0);
            }

            return null;
        }

        protected void onPostExecute(Address addresss) {
            if (addresss != null) {
                String address = addresss.getAddressLine(0);
                if(homeAddress == null)
                {
                    homeAddress = address;
                }
                sendBroadcastMessage("",address);
            }
            else {
                sendBroadcastMessage("","Unknown Location");
            }
        }
    }

//    private boolean isSameProvider(String provider1, String provider2) {
//        if (provider1 == null) {
//            return provider2 == null;
//        }
//        return provider1.equals(provider2);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class MyLocationListener implements android.location.LocationListener {

        public void onLocationChanged(final Location loc) {
            try {
                if(defaultLocation != null) {
                    if (checkDeviceIsMoved(loc, defaultLocation)) {
                        if (isConnected()) {
//                        getDistance((mLastLocation.getLatitude()), mLastLocation.getLongitude(), loc.getLatitude(), loc.getLongitude());
                            new GeocodeAsyncTask().execute(loc.getLatitude(), loc.getLongitude());
                        } else {
                            sendBroadcastMessage("", "Device is not Connected to Internet");
                        }
                    } else {
                        sendBroadcastMessage("", "Challenge Break");
//                        stopMyService();
                    }
                }
                else {
                    defaultLocation = loc;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void onProviderDisabled(String provider) {
            // Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            // Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
}
