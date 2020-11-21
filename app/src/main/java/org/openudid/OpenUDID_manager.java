package org.openudid;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class OpenUDID_manager implements ServiceConnection {
    private static final boolean LOG = true;
    private static String OpenUDID = null;
    public static final String PREFS_NAME = "openudid_prefs";
    public static final String PREF_KEY = "openudid";
    public static final String TAG = "OpenUDID";
    private static boolean mInitialized = false;
    private final Context mContext;
    private List<ResolveInfo> mMatchingIntents;
    private final SharedPreferences mPreferences;
    private final Random mRandom = new Random();
    private final Map<String, Integer> mReceivedOpenUDIDs = new HashMap();

    public void onServiceDisconnected(ComponentName componentName) {
    }

    private OpenUDID_manager(Context context) {
        this.mPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        this.mContext = context;
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        String readString;
        try {
            Parcel obtain = Parcel.obtain();
            obtain.writeInt(this.mRandom.nextInt());
            Parcel obtain2 = Parcel.obtain();
            iBinder.transact(1, Parcel.obtain(), obtain2, 0);
            if (obtain.readInt() == obtain2.readInt() && (readString = obtain2.readString()) != null) {
                Log.d(TAG, "Received " + readString);
                if (this.mReceivedOpenUDIDs.containsKey(readString)) {
                    this.mReceivedOpenUDIDs.put(readString, Integer.valueOf(this.mReceivedOpenUDIDs.get(readString).intValue() + 1));
                } else {
                    this.mReceivedOpenUDIDs.put(readString, 1);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException: " + e.getMessage());
        }
        this.mContext.unbindService(this);
        startService();
    }

    private void storeOpenUDID() {
        SharedPreferences.Editor edit = this.mPreferences.edit();
        edit.putString(PREF_KEY, OpenUDID);
        edit.apply();
    }

    @SuppressLint({"HardwareIds"})
    private void generateOpenUDID() {
        Log.d(TAG, "Generating openUDID");
        OpenUDID = Settings.Secure.getString(this.mContext.getContentResolver(), "android_id");
        String str = OpenUDID;
        if (str == null || str.equals("9774d56d682e549c") || OpenUDID.length() < 15) {
            OpenUDID = new BigInteger(64, new SecureRandom()).toString(16);
        }
    }

    private void startService() {
        if (this.mMatchingIntents.size() > 0) {
            Log.d(TAG, "Trying service " + ((Object) this.mMatchingIntents.get(0).loadLabel(this.mContext.getPackageManager())));
            ServiceInfo serviceInfo = this.mMatchingIntents.get(0).serviceInfo;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(serviceInfo.applicationInfo.packageName, serviceInfo.name));
            this.mMatchingIntents.remove(0);
            try {
                this.mContext.bindService(intent, this, 1);
            } catch (SecurityException unused) {
                startService();
            }
        } else {
            getMostFrequentOpenUDID();
            if (OpenUDID == null) {
                generateOpenUDID();
            }
            Log.d(TAG, "OpenUDID: " + OpenUDID);
            storeOpenUDID();
            mInitialized = true;
        }
    }

    private void getMostFrequentOpenUDID() {
        if (!this.mReceivedOpenUDIDs.isEmpty()) {
            TreeMap treeMap = new TreeMap(new ValueComparator());
            treeMap.putAll(this.mReceivedOpenUDIDs);
            OpenUDID = (String) treeMap.firstKey();
        }
    }

    public static String getOpenUDID() {
        if (!mInitialized) {
            Log.e(TAG, "Initialisation isn't done");
        }
        return OpenUDID;
    }

    public static boolean isInitialized() {
        return mInitialized;
    }

    public static void sync(Context context) {
        OpenUDID_manager openUDID_manager = new OpenUDID_manager(context);
        OpenUDID = openUDID_manager.mPreferences.getString(PREF_KEY, null);
        if (OpenUDID == null) {
            openUDID_manager.mMatchingIntents = context.getPackageManager().queryIntentServices(new Intent("org.OpenUDID.GETUDID"), 0);
            Log.d(TAG, openUDID_manager.mMatchingIntents.size() + " services matches OpenUDID");
            if (openUDID_manager.mMatchingIntents != null) {
                openUDID_manager.startService();
                return;
            }
            return;
        }
        Log.d(TAG, "OpenUDID: " + OpenUDID);
        mInitialized = true;
    }

    /* access modifiers changed from: private */
    public class ValueComparator implements Comparator {
        private ValueComparator() {
        }

        @Override // java.util.Comparator
        public int compare(Object obj, Object obj2) {
            if (((Integer) OpenUDID_manager.this.mReceivedOpenUDIDs.get(obj)).intValue() < ((Integer) OpenUDID_manager.this.mReceivedOpenUDIDs.get(obj2)).intValue()) {
                return 1;
            }
            return OpenUDID_manager.this.mReceivedOpenUDIDs.get(obj) == OpenUDID_manager.this.mReceivedOpenUDIDs.get(obj2) ? 0 : -1;
        }
    }
}
