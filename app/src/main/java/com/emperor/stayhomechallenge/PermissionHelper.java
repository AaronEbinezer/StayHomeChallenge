package com.emperor.stayhomechallenge;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SimpleArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// Helper function to check Permission and request Permission

public class PermissionHelper {

    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static final String LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final Context mContext;
    private final Object mType;
    private final String[] mRequiredPermissions;
    private final List<String> mPermissionsToRequest = new ArrayList<>();
    private static final SimpleArrayMap<Integer,PermissionListener> mPermissionListeners=new SimpleArrayMap<>();
    private static final SimpleArrayMap<Integer,GPSListener> mGPSListener=new SimpleArrayMap<>();

    private static LocationRequest mLocationRequest;
    private static LocationSettingsRequest.Builder mLocationSettingsRequestBuilder;
    private static int locationRequestCode = 1000;
    /**    Type will be Activity or fragment
     *
     * @param type
     */
    public PermissionHelper(Object type) {
        mType = type;
        if (type instanceof Activity) {
            mContext = (Context) type;
        } else if (type instanceof Fragment) {
            mContext = ((Fragment) type).getContext();
        } else {
            throw new RuntimeException("Please pass Activity or Fragment as a type");
        }

        if (!isBelowLollipop()) {
            mRequiredPermissions = getPermissionsFromManifest(mContext);
        } else {
            mRequiredPermissions = null;
        }

    }
    //
    public static final void requestPermission(AppCompatActivity activity, String permission, int requestCode, PermissionListener permissionListener){
        ActivityCompat.requestPermissions(activity,new String[]{permission},requestCode);
        mPermissionListeners.put(requestCode,permissionListener);
    }

    public static final void requestPermission(Fragment fragment, String permission, int requestCode, PermissionListener permissionListener){
        fragment.requestPermissions(new String[]{permission},requestCode);
        mPermissionListeners.put(requestCode,permissionListener);
    }

    public static final void requestGPS(Activity activity,int requestCode){
        locationSetting(activity,requestCode);

    }
    public static final void checkPermissionResult(Activity appActivity, int requestCode,
                                                   String[] permissions, int[] grantResults){
        final PermissionListener permissionListener = mPermissionListeners.get(requestCode);
        mPermissionListeners.remove(requestCode);
        if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(appActivity,permissions[0]);
            if(showRationale){
                if (permissionListener != null) {
                    permissionListener.onPermissionStatus(PermissionListener.STATUS_DENIED);
                }
            }else{
                if (permissionListener != null) {
                    permissionListener.onPermissionStatus(PermissionListener.STATUS_DISABLED);
                }
            }
        }else{
            if (permissionListener != null) {
                permissionListener.onPermissionStatus(PermissionListener.STATUS_GRANTED);
            }
        }
    }

    /**    It will check all the required permissions or granted
     *
     * @return
     */
    public boolean hasPermissions() {
        if (isBelowLollipop()) return true;

        mPermissionsToRequest.clear();

        if (mRequiredPermissions == null) return true;

        for (String permission : mRequiredPermissions) {
            if (isPermissionNotAvailable(permission)) {
                mPermissionsToRequest.add(permission);
            }
        }

        return mPermissionsToRequest.isEmpty();
    }

    /**    This method will check the passed permission is granted or not
     *
     * @param permission
     * @return
     */
    public boolean isPermissionNotAvailable(String permission) {
        return !isPermissionAvailable(permission);
    }

    public boolean isPermissionAvailable(String permission){
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPermissionAvailable(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**    It will check for all the permissions are already if shown it will show the permission feature dialog
     *
     * @param yesClickListener
     * @param cancelListener
     */
    public void  shouldShowRelationale(Dialog.OnClickListener yesClickListener, Dialog.OnClickListener cancelListener) {
        Activity activity = null;
        Fragment fragment = null;

        if (mType instanceof AppCompatActivity) {
            activity = (Activity) mType;
        } else if (mType instanceof Fragment) {
            fragment = (Fragment) mType;
        }

        for (int i = 0, N = mPermissionsToRequest.size(); i < N; i++) {
            if (activity != null) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermissionsToRequest.get(i))) {
                    new AlertDialog.Builder(activity, R.style.AppTheme)
                            .setMessage("Without the permission " + mPermissionsToRequest.get(i) + " this app cannot work")
                            .setTitle("Permission Needed")
                            .setPositiveButton("Ok", yesClickListener)
                            .setNegativeButton("No", cancelListener)
                            .setCancelable(false)
                            .show();
                    return;
                }
            } else {
                if (fragment != null && fragment.shouldShowRequestPermissionRationale(mPermissionsToRequest.get(i))) {
                    new AlertDialog.Builder(fragment.getContext(), R.style.AppTheme)
                            .setMessage("Without the permission " + mPermissionsToRequest.get(i) + " this app cannot work")
                            .setTitle("Permission Needed")
                            .setPositiveButton("Ok", yesClickListener)
                            .setNegativeButton("No", cancelListener)
                            .setCancelable(false)
                            .show();
                    return;
                }
            }
        }
    }

    /**    It will check the permission is denied and disabled by the user
     *
     * @param permission
     * @return
     */
    public boolean isPermissionDisabled(String permission) {

        Activity activity = null;
        Fragment fragment = null;

        if (mType instanceof AppCompatActivity) {
            activity = (Activity) mType;
        } else if (mType instanceof Fragment) {
            fragment = (Fragment) mType;
        }

        if (activity != null) {
            if (isPermissionNotAvailable(permission) && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        } else if(fragment!=null){
            if (isPermissionNotAvailable(permission) && !fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }

        return false;
    }

    /**    It will return all the required permission (or) manifest declared permissions
     *
     * @return
     */
    public String[] getRequiredPermissions() {
        return mRequiredPermissions;
    }

    /**    It will return all the requested or not granted permissions available in the manifest
     *
     * @return
     */
    public String[] getRequestedPermissions() {
        return mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);
    }

    /**    It will check the current version of device is below lollipop
     *
     * @return
     */
    private boolean isBelowLollipop() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**    It will open the request permission dialog
     *
     * @param requestCode
     */
    public void requestAllPermission(int requestCode) {
        requestAllPermission(requestCode,null);
    }

    /**    It will open the request permission dialog
     *
     * @param requestCode
     */
    public void requestAllPermission(int requestCode, PermissionListener permissionListener) {
        if(mPermissionListeners.get(requestCode) != null){
            mPermissionListeners.remove(requestCode);
        }
        mPermissionListeners.put(requestCode,permissionListener);
        if (isBelowLollipop()) return;

        if (hasPermissions()) return;

        String[] request = mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);

        if (mType instanceof AppCompatActivity) {
            ActivityCompat.requestPermissions((Activity) mType, request, requestCode);
        } else if (mType instanceof Fragment) {
            ((Fragment) mType).requestPermissions(request, requestCode);
        }
    }

    /**    It will get all the required or declared permissions in the manifest
     *
     * @param context
     * @return
     */
    private String[] getPermissionsFromManifest(Context context) {

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            return info.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("This should have never happened. While requesting permissions.", e);
        }
    }

    /**    It will check for the permission and grant results
     *
     * @param permissions
     * @param grantResults
     * @return
     */
    public boolean areAllRequiredPermissionsGranted(String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0
                || grantResults == null || grantResults.length == 0) {
            return false;
        }

        LinkedHashMap<String, Integer> perms = new LinkedHashMap<>();

        for (int i = 0, N = permissions.length; i < N; i++) {
            if (!perms.containsKey(permissions[i])
                    || (perms.containsKey(permissions[i]) && perms.get(permissions[i]) == PackageManager.PERMISSION_DENIED))
                perms.put(permissions[i], grantResults[i]);
        }

        for (Map.Entry<String, Integer> entry : perms.entrySet()) {
            if (entry.getValue() != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    //To enable GPS//
    public static void locationSetting(final Activity mActivity, int requestCode) {
        final GPSListener gpsListener = mGPSListener.get(requestCode);
        mLocationRequest=new LocationRequest()
                .setFastestInterval(1500)
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationSettingsRequestBuilder=new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> mLocationSettingsResponse= LocationServices.getSettingsClient(mActivity).checkLocationSettings(mLocationSettingsRequestBuilder.build());

        mLocationSettingsResponse.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                }catch (ApiException e){
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.SUCCESS:
                            gpsListener.onGPSStatus(GPSListener.STATUS_GPS_ON);
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException=(ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(mActivity,locationRequestCode );
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.CANCELED:
                            gpsListener.onGPSStatus(GPSListener.STATUS_GPS_DENIED);
                            break;
                    }
                }
            }
        });
    }

    public static void openSettings(Context context){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public interface PermissionListener{
        int STATUS_GRANTED  = 1;
        int STATUS_DENIED   = 2;
        int STATUS_DISABLED = 3;
        void onPermissionStatus(int status);
    }

    public interface GPSListener{
        int STATUS_GPS_ON    =0;
        int STATUS_GPS_DENIED   = 1;
        void onGPSStatus(int status);
    }
}
