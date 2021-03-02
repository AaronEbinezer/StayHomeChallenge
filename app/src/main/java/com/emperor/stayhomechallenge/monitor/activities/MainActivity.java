package com.emperor.stayhomechallenge.monitor.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emperor.stayhomechallenge.BuildConfig;
import com.emperor.stayhomechallenge.Constants;
import com.emperor.stayhomechallenge.R;
import com.emperor.stayhomechallenge.alert.AlertBox;
import com.emperor.stayhomechallenge.monitor.services.BGServicenormal;
import com.emperor.stayhomechallenge.network.NetworkConnection;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.emperor.stayhomechallenge.Constants.CURRENT_ADDRESS_KEY;
import static com.emperor.stayhomechallenge.Constants.DISTANCE_KEY;
import static com.emperor.stayhomechallenge.Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.emperor.stayhomechallenge.Constants.GPS_SERVICE_VALUE;
import static com.emperor.stayhomechallenge.Constants.HOME_ADDRESS_KEY;
import static com.emperor.stayhomechallenge.Constants.TIMER_KEY;
import static com.emperor.stayhomechallenge.Constants.UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.emperor.stayhomechallenge.Constants.editor;
import static com.emperor.stayhomechallenge.Constants.sharePref;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private boolean permissionGranted=true;
    private LocationRequest mLocationRequest;
    private BroadcastReceiver localBroadCastReceiver = null;
    private TextView tvTimer, tvStart, tvStop, tvCurrentLocation, tvHomeLocation, tvDistance, tvMsg;
    private Intent intent;
    private ViewGroup llContainerLayout;
    private LinearLayout llLocationLayout, lvLineLayout;
    private int mInterval = 1000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: ");
                return;
                //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        startRepeatingTask();
        Log.i(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        llContainerLayout = findViewById(R.id.ll_main_container);
        llLocationLayout = llContainerLayout.findViewById(R.id.ll_location_layout);
        lvLineLayout = llContainerLayout.findViewById(R.id.ll_line_layout);
        tvCurrentLocation = findViewById(R.id.tv_location);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);
        tvTimer = findViewById(R.id.tv_timer);
        tvHomeLocation = findViewById(R.id.tv_home_location);
        tvDistance = findViewById(R.id.tv_distance);
        tvMsg = findViewById(R.id.tv_msg);

        Constants.setSharePreference(MainActivity.this);

        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        tvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY,false);
                setLogic(false,"","This is the Patient Time you been in your Home");
                stopCustomService();
            }
        });


        boolean isServiceRunning = sharePref.getBoolean(Constants.IS_SERVICE_RUNNING_KEY,false);
        String gpsService = getIntent().getStringExtra(Constants.GPS_OFF_KEY);

        if(gpsService != null && GPS_SERVICE_VALUE.equals(gpsService))
        {
            setLogic(isServiceRunning,getIntent().getStringExtra(TIMER_KEY),"");
        }
        else setLogic(isServiceRunning,"","");


        askBatteryOptimization();

    }

    private void checkPermission()
    {
        NetworkConnection connection = new NetworkConnection(this);
        if(connection.checkInternet()) {
            initPermission();
        }
        else {
            AlertBox alertBox = new AlertBox(this);
            alertBox.showAlertBoxWithListener(getString(R.string.no_internet_connection),View.GONE);
            alertBox.setOnPositiveClickListener(new AlertBox.OnPositiveClickListener() {
                @Override
                public void onPositiveClick() {
                    finish();
                }
            });

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        if(localBroadCastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadCastReceiver);
        String currentAddress = tvCurrentLocation.getText().toString().trim();
        String homeAddress = tvHomeLocation.getText().toString().trim();
        String distance = tvDistance.getText().toString().trim();
        if(!TextUtils.isEmpty(currentAddress))
        {
            Constants.setStringEditor(CURRENT_ADDRESS_KEY, currentAddress);
        }
        if(!TextUtils.isEmpty(homeAddress))
        {
            Constants.setStringEditor(HOME_ADDRESS_KEY, homeAddress);
        }
        if(!TextUtils.isEmpty(distance))
        {
            Constants.setStringEditor(DISTANCE_KEY, distance);
        }

    }

    public class LocalBroadCastRegister extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra(TIMER_KEY);
            String currentAddress = intent.getStringExtra(CURRENT_ADDRESS_KEY);
            String homeAddress = intent.getStringExtra(HOME_ADDRESS_KEY);
            double distance = intent.getDoubleExtra(DISTANCE_KEY, 0);
            if(currentAddress.equals(Constants.GPS_OFF_KEY))
            {
                setLogic(false,"","You have Switched Off your GPS");
                stopCustomService();
            }
            else if(!currentAddress.equals("Challenge Break")) {
                if (!TextUtils.isEmpty(time))
                    tvTimer.setText(time);
                if (!TextUtils.isEmpty(currentAddress))
                    tvCurrentLocation.setText(currentAddress);
                if (!TextUtils.isEmpty(homeAddress))
                    tvHomeLocation.setText(homeAddress);
                    tvDistance.setText(distance+"");
            }
            else {
                Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY, false);
                setLogic(false,"","You have crossed the Limit, Distance: "+distance);
                stopCustomService();
//                tvMsg.setText("You have crossed the Limit, Distance: "+distance);
//                tvMsg.setVisibility(View.VISIBLE);
                tvCurrentLocation.setText("");
                tvHomeLocation.setText("");
                tvDistance.setText("");
            }
        }
    }

    private void initPermission() {
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
        else {
            permissionGranted=true;
            init();
            startLocationUpdate();
        }
    }

    private void init() {
        if(permissionGranted) {
            mSettingsClient = LocationServices.getSettingsClient(this);
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();
//        startLocationButtonClick();
        }
    }

    // first time permissions for location
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0) {
            int len=  permissions.length;
            for (int i = 0;i <len; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted=false;
                }
            }

            if(permissionGranted)
            {
                init();
                startLocationUpdate();
            }
            else {

                AlertBox alertBox = new AlertBox(MainActivity.this);
                alertBox.showAlertBoxWithListener("You did not give permission to access your Location. Do want to exit",View.VISIBLE);

                alertBox.setOnPositiveClickListener(new AlertBox.OnPositiveClickListener() {
                    @Override
                    public void onPositiveClick() {
                        finish();
                    }
                });

                alertBox.setOnNegativeClickListener(new AlertBox.OnNegativeClickListener() {
                    @Override
                    public void onPositiveClick() {
                        permissionGranted=true;
                        requestManualPermission(permissions,requestCode);
                    }
                });
            }
        }

    }

    private void requestManualPermission(String[] permissions, int requestCode)
    {
        ActivityCompat.requestPermissions(this,permissions,requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        if (permissionGranted) {
                            startLocationUpdate();
                        } else {
                            initPermission();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User choose not to make required location settings changes.");
                        AlertBox alertBox = new AlertBox(this);
                        alertBox.showAlertBoxWithListener(getString(R.string.gps_error), View.VISIBLE);
                        alertBox.setOnPositiveClickListener(new AlertBox.OnPositiveClickListener() {
                            @Override
                            public void onPositiveClick() {
                                startLocationUpdate();
                            }
                        });
                        alertBox.setOnNegativeClickListener(new AlertBox.OnNegativeClickListener() {
                            @Override
                            public void onPositiveClick() {
                                finish();
                            }
                        });
                        break;
                }
                break;
        }
    }

    private void startLocationUpdate() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        if(sharePref.getBoolean("autoStart",true))
                        {
                            getAutoStartForApp();
                        }
                        setLogic(true,"","");
                        startCustomService();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                break;
                            case LocationSettingsStatusCodes.SUCCESS:
                                if(sharePref.getBoolean("autoStart",true))
                                {
                                    getAutoStartForApp();
                                }
                                permissionGranted=true;
                                init();
                        }
                    }
                });
    }

    private void setLogic(boolean isServiceRunning, String stTimer, String stMsg)
    {
        if(isServiceRunning)
        {
            tvStart.setVisibility(View.GONE);
            tvStop.setVisibility(View.VISIBLE);
            TransitionManager.beginDelayedTransition(llContainerLayout);
            llLocationLayout.setVisibility(View.VISIBLE);
            lvLineLayout.setVisibility(View.VISIBLE);
//            String getNotificationContent= getIntent().getStringExtra(Constants.INTENT_CODE);
//            if(getNotificationContent != null)
//            {
            localBroadCastReceiver = new LocalBroadCastRegister();
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    localBroadCastReceiver, new IntentFilter(Constants.TIMER_INTENT)
            );
//            }

            String currentAddress = sharePref.getString(CURRENT_ADDRESS_KEY, "");
            String homeAddress = sharePref.getString(HOME_ADDRESS_KEY, "");
            String distance = sharePref.getString(DISTANCE_KEY, "");
            if (!TextUtils.isEmpty(currentAddress)) {
                tvCurrentLocation.setText(currentAddress);
            }
            if (!TextUtils.isEmpty(homeAddress)) {
                tvHomeLocation.setText(homeAddress);
            }
            if (!TextUtils.isEmpty(distance)) {
                tvDistance.setText(distance);
            }
        }
        else
        {
            tvStart.setVisibility(View.VISIBLE);
            tvStop.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(llContainerLayout);
            llLocationLayout.setVisibility(View.GONE);
            lvLineLayout.setVisibility(View.GONE);
            if(!TextUtils.isEmpty(stMsg)) {
                tvMsg.setVisibility(View.VISIBLE);
                tvMsg.setText(stMsg);
            }
            if(!TextUtils.isEmpty(stTimer))
            {
                tvTimer.setText(stTimer);
            }
        }
    }



    private void startCustomService()
    {
        Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY, true);
//        localBroadCastReceiver = new LocalBroadCastRegister();
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                localBroadCastReceiver, new IntentFilter(Constants.TIMER_INTENT)
//        );
        Intent intent = new Intent(MainActivity.this, BGServicenormal.class);
        ContextCompat.startForegroundService(MainActivity.this,intent);
//        getApplicationContext().registerReceiver(new GpsBroadCastListener(), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        tvMsg.setVisibility(View.GONE);
    }

    private void stopCustomService()
    {
        Constants.setBooleanEditor(Constants.IS_SERVICE_RUNNING_KEY, false);
        if(intent !=null)
            stopService(intent);
        else {
            intent = new Intent(MainActivity.this, BGServicenormal.class);
            stopService(intent);
        }
        if(localBroadCastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(
                    localBroadCastReceiver
            );
    }

    private void getAutoStartForApp() {

        try {

            Intent intent = new Intent();

            String manufacturer = Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));

            } else if ("oppo".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));

            } else if ("vivo".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));

            } else if ("Letv".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));

            } else if ("Honor".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));

            }  else if ("realme".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));

            } else if ("asus".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings"));

            }else if ("nokia".equalsIgnoreCase(manufacturer)) {

                intent.setComponent(new ComponentName("com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"));

            }

//            else {
//
//
//            }

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (list.size() > 0) {
                getAutoStartPermission(intent);
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
    private void getAutoStartPermission(final Intent intent){
        AlertBox alert=new AlertBox(MainActivity.this);
        alert.showAlertBoxWithListener("Please enable autostart option to use this Application efficiently",View.GONE);
        alert.setOnPositiveClickListener(new AlertBox.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                editor.putBoolean(Constants.autoStartKey, false);
                editor.commit();
                startActivity(intent);
            }
        });
    }
    private void askBatteryOptimization() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                intent.setData(Uri.parse("package:" + getPackageName()));
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

}
