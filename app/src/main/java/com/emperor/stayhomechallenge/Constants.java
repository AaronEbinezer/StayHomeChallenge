package com.emperor.stayhomechallenge;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    public static SharedPreferences sharePref;
    public static SharedPreferences.Editor editor;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS              =             5000;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS                      =             5000;
    private final static String SHARED_CONFIG                                     =             "StayHomeChallenge@94";
    public static String autoStartKey                                             =             "autoStart";
    public static String INTENT_CODE                                              =             "service";
    public static String TIMER_KEY                                                =             "receiver";
    public static String CURRENT_ADDRESS_KEY                                      =             "currentAddress";
    public static String HOME_ADDRESS_KEY                                         =             "address";
    public static String DISTANCE_KEY                                             =             "distance";
    public static String TIMER_INTENT                                             =             "actionBroadCast";
    public static String IS_SERVICE_RUNNING_KEY                                   =             "isServiceRunning";
    public static String GPS_OFF_KEY                                              =             "gpsSwitchedOff";
    public static String GPS_SERVICE_VALUE                                        =             "gpsService";
    public static int TIMER_RESULT_CODE                                           =             99;


    public static SharedPreferences setSharePreference(Context context)
    {
        sharePref = context.getSharedPreferences(SHARED_CONFIG, Context.MODE_PRIVATE);
        editor = sharePref.edit();
        return sharePref;
    }

    public static void setStringEditor(String key, String value)
    {
        editor.putString(key, value);
        editor.commit();
    }

    public static void setBooleanEditor(String key, boolean value)
    {
        editor.putBoolean(key, value);
        editor.commit();
    }


}
