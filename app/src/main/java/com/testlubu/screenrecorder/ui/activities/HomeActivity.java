package com.testlubu.screenrecorder.ui.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
//import com.ads.control.AdmobHelp;
import com.ads.control.Rate;
import com.google.android.material.navigation.NavigationView;
import com.testlubu.screenrecorder.BaseActivity;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.interfaces.PermissionResultListener;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.ui.fragments.BaseFragment;
import com.testlubu.screenrecorder.ui.fragments.ScreenshotsListFragment;
import com.testlubu.screenrecorder.ui.fragments.SettingsFragment;
import com.testlubu.screenrecorder.ui.fragments.VideosEditedListFragment;
import com.testlubu.screenrecorder.ui.fragments.VideosListFragment;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    private FragmentManager fragmentManager;
    private PermissionResultListener mPermissionResultListener;
    private ScreenshotsListFragment mScreenshotFragment;
    private SettingsFragment mSettingsFragment;
    private FragmentTransaction mTransaction;
    private VideosEditedListFragment mVideosEditedFragment;
    private VideosListFragment mVideosFragment;
    private SharedPreferences prefs;

    private void backFragment() {
    }

    private void initEvents() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void onDirectoryChanged() {
    }

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, com.testlubu.screenrecorder.BaseActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_home);
        try {
//            AdmobHelp.getInstance().loadBanner(this);
            initViews();
            initEvents();
            addVideoFragment();
            this.mSettingsFragment = SettingsFragment.newInstance();
            if (getIntent() != null && getIntent().getExtras().containsKey("action") && getIntent().getExtras().get("action").equals("setting")) {
                addSettingsFragment();
            }
        } catch (Exception unused) {
        }
    }

    private void initViews() {
        try {
            this.fragmentManager = getFragmentManager();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setItemIconTintList(null);
            navigationView.setNavigationItemSelectedListener(this);
        } catch (Exception unused) {
        }
    }

    public static boolean hasPermissions(Context context, String... strArr) {
        if (context == null || strArr == null) {
            return true;
        }
        for (String str : strArr) {
            if (ActivityCompat.checkSelfPermission(context, str) != 0) {
                return false;
            }
        }
        return true;
    }

    public void setPermissionResultListener(PermissionResultListener permissionResultListener) {
        this.mPermissionResultListener = permissionResultListener;
    }

    public boolean requestPermissionStorage() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        new AlertDialog.Builder(this).setTitle(getString(R.string.storage_permission_request_title)).setMessage(getString(R.string.storage_permission_request_summary)).setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.activities.HomeActivity.AnonymousClass2 */

            public void onClick(DialogInterface dialogInterface, int i) {
                HomeActivity homeActivity = HomeActivity.this;
                if (!HomeActivity.hasPermissions(homeActivity, homeActivity.PERMISSIONS)) {
                    HomeActivity homeActivity2 = HomeActivity.this;
                    ActivityCompat.requestPermissions(homeActivity2, homeActivity2.PERMISSIONS, Const.EXTDIR_REQUEST_CODE);
                }
            }
        }).setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.activities.HomeActivity.AnonymousClass1 */

            public void onClick(DialogInterface dialogInterface, int i) {
                HomeActivity.this.finish();
            }
        }).setCancelable(false).create().show();
        return false;
    }

    private void addFragment(final Fragment fragment) {
        try {
            this.mTransaction = this.fragmentManager.beginTransaction();
            this.mTransaction.replace(R.id.fragment_content, fragment);
            this.mTransaction.commit();
            if (fragment instanceof BaseFragment) {
                new Handler().postDelayed(new Runnable() {
                    /* class com.testlubu.screenrecorder.ui.activities.HomeActivity.AnonymousClass3 */

                    public void run() {
                        HomeActivity.this.runOnUiThread(new Runnable() {
                            /* class com.testlubu.screenrecorder.ui.activities.HomeActivity.AnonymousClass3.AnonymousClass1 */

                            public void run() {
                                ((BaseFragment) fragment).onVisibleFragment();
                            }
                        });
                    }
                }, 100);
            }
        } catch (Exception unused) {
        }
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Rate.Show(this, 1);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        menuItem.getItemId();
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        try {
            switch (menuItem.getItemId()) {
                case R.id.nav_gallery /*{ENCODED_INT: 2131362050}*/:
                    addScreenshotFragment();
                    break;
                case R.id.nav_more /*{ENCODED_INT: 2131362051}*/:
                    Utils.openURL(this, getResources().getString(R.string.link_more_app));
                    break;
                case R.id.nav_policy /*{ENCODED_INT: 2131362052}*/:
                    Utils.openURL(this, getResources().getString(R.string.link_policy));
                    break;
                case R.id.nav_settings /*{ENCODED_INT: 2131362053}*/:
                    addSettingsFragment();
                    break;
                case R.id.nav_share /*{ENCODED_INT: 2131362054}*/:
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.SEND");
                    intent.putExtra("android.intent.extra.TEXT", Utils.getAppUrl(this));
                    intent.setType("text/plain");
                    startActivity(intent);
                    break;
                case R.id.nav_video /*{ENCODED_INT: 2131362056}*/:
                    addVideoFragment();
                    break;
            }
            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            return true;
        } catch (Exception unused) {
            return true;
        }
    }

    private void addVideoEditedFragment() {
        if (this.mVideosEditedFragment == null) {
            this.mVideosEditedFragment = VideosEditedListFragment.newInstance();
        }
        addFragment(this.mVideosEditedFragment);
    }

    private void addVideoFragment() {
        if (this.mVideosFragment == null) {
            this.mVideosFragment = VideosListFragment.newInstance();
        }
        addFragment(this.mVideosFragment);
    }

    private void addScreenshotFragment() {
        if (this.mScreenshotFragment == null) {
            this.mScreenshotFragment = ScreenshotsListFragment.newInstance();
        }
        addFragment(this.mScreenshotFragment);
    }

    private void addSettingsFragment() {
        if (this.mSettingsFragment == null) {
            this.mSettingsFragment = SettingsFragment.newInstance();
        }
        addFragment(this.mSettingsFragment);
    }

    @TargetApi(23)
    public void requestSystemWindowsPermission(int i) {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())), i);
        }
    }

    @TargetApi(23)
    private void setSystemWindowsPermissionResult(int i) {
        if (Build.VERSION.SDK_INT < 23) {
            this.mPermissionResultListener.onPermissionResult(i, new String[]{"System Windows Permission"}, new int[]{0});
        } else if (Settings.canDrawOverlays(this)) {
            this.mPermissionResultListener.onPermissionResult(i, new String[]{"System Windows Permission"}, new int[]{0});
        } else {
            this.mPermissionResultListener.onPermissionResult(i, new String[]{"System Windows Permission"}, new int[]{-1});
        }
    }

    public void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, Const.CAMERA_REQUEST_CODE);
        }
    }

    public void requestPermissionAudio(int i) {
        if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.RECORD_AUDIO"}, i);
        }
    }

    @Override // androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback, androidx.fragment.app.FragmentActivity
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1110) {
            if (iArr.length <= 0 || iArr[0] == 0) {
                startService();
                Log.d(Const.TAG, "write storage Permission granted");
                Utils.createDir();
            } else {
                Log.d(Const.TAG, "write storage Permission Denied");
            }
        }
        PermissionResultListener permissionResultListener = this.mPermissionResultListener;
        if (permissionResultListener != null) {
            permissionResultListener.onPermissionResult(i, strArr, iArr);
        }
    }

    public void startService() {
        startService(new Intent(this, FloatingControlService.class));
    }
}
