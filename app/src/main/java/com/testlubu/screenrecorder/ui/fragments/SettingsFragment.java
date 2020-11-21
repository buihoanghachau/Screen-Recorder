package com.testlubu.screenrecorder.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.exifinterface.media.ExifInterface;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.folderpicker.FolderChooserDialog;
import com.testlubu.screenrecorder.folderpicker.OnDirectorySelectedListerner;
import com.testlubu.screenrecorder.interfaces.PermissionResultListener;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.listener.HideService;
import com.testlubu.screenrecorder.model.listener.ShowService;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import com.testlubu.screenrecorder.ui.activities.ShowTouchTutsActivity;
import com.testlubu.screenrecorder.ui.dialog.AppPickerDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public class SettingsFragment extends Fragment implements PermissionResultListener, OnDirectorySelectedListerner, View.OnClickListener {
    private HomeActivity activity;
    private AppPickerDialog appPickerDialog;
    private SwitchCompat cbCamera;
    private SwitchCompat cbFloatControls;
    private SwitchCompat cbSavingGif;
    private SwitchCompat cbShark;
    private SwitchCompat cbTargetApp;
    private SwitchCompat cbTouches;
    private SwitchCompat cbVibrate;
    private FolderChooserDialog folderChooserDialog;
    private View mRootView;
    private SharedPreferences prefs;
    String[] resEntries;
    String[] resEntryValues;

    private float bitsToMb(float f) {
        return f / 1048576.0f;
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        this.mRootView = layoutInflater.inflate(R.layout.fragment_settings, viewGroup, false);
        this.mRootView.setBackgroundColor(getResources().getColor(R.color.globalWhite));
        initViews();
        initEvents();
        return this.mRootView;
    }

    private void initEvents() {
        this.mRootView.findViewById(R.id.layout_vibrate).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_language).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_timer).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_resolution).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_frams).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_bit_rate).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_orientation).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_audio).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_location).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_name_format).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_name_prefix).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_use_float_controls).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_show_touches).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_camera_overlay).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_enable_target_app).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_choose_app).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_enable_saving_in_gif).setOnClickListener(this);
        this.mRootView.findViewById(R.id.layout_shark).setOnClickListener(this);
        this.cbFloatControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass1 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    try {
                        SettingsFragment.this.requestSystemWindowsPermission(Const.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE);
                        if (FloatingControlService.getInstance() == null) {
                            ((HomeActivity) SettingsFragment.this.getActivity()).startService();
                        }
                        ObserverUtils.getInstance().notifyObservers(new ShowService());
                    } catch (Exception unused) {
                        return;
                    }
                } else {
                    ObserverUtils.getInstance().notifyObservers(new HideService());
                }
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_floating_control_key), z);
            }
        });
        this.cbTouches.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass2 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_show_touch_key), z);
            }
        });
        this.cbCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass3 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    SettingsFragment.this.requestCameraPermission();
                    SettingsFragment.this.requestSystemWindowsPermission(Const.CAMERA_SYSTEM_WINDOWS_CODE);
                }
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_camera_overlay_key), z);
            }
        });
        this.cbTargetApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass4 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_enable_target_app_key), z);
                SettingsFragment.this.mRootView.findViewById(R.id.layout_choose_app).setEnabled(z);
            }
        });
        this.cbSavingGif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass5 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_save_gif_key), z);
            }
        });
        this.cbShark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass6 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_shake_gesture_key), z);
            }
        });
        this.cbVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass7 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.preference_vibrate_key), z);
            }
        });
    }

    private void initViews() {
        setPermissionListener();
        Activity activity2 = getActivity();
        String string = getString(R.string.savelocation_key);
        String readStringValue = PrefUtils.readStringValue(activity2, string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.folderChooserDialog = new FolderChooserDialog(getActivity());
        this.folderChooserDialog.setOnDirectoryClickedListerner(this);
        this.folderChooserDialog.setCurrentDir(readStringValue);
        this.folderChooserDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass8 */

            public void onDismiss(DialogInterface dialogInterface) {
                FolderChooserDialog unused = SettingsFragment.this.folderChooserDialog;
                FolderChooserDialog.onDirectorySelectedListerner.onDirectorySelected();
            }
        });
        this.cbFloatControls = (SwitchCompat) this.mRootView.findViewById(R.id.cb_use_float_controls);
        this.cbTouches = (SwitchCompat) this.mRootView.findViewById(R.id.cb_show_touches);
        this.cbCamera = (SwitchCompat) this.mRootView.findViewById(R.id.cb_camera_overlay);
        this.cbTargetApp = (SwitchCompat) this.mRootView.findViewById(R.id.cb_enable_target_app);
        this.cbSavingGif = (SwitchCompat) this.mRootView.findViewById(R.id.cb_saving_in_gif);
        this.cbShark = (SwitchCompat) this.mRootView.findViewById(R.id.cb_shark);
        this.cbVibrate = (SwitchCompat) this.mRootView.findViewById(R.id.cb_vibrate);
        this.cbFloatControls.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_floating_control_key), true));
        this.cbTouches.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_show_touch_key), false));
        this.cbCamera.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_camera_overlay_key), false));
        this.cbTargetApp.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_enable_target_app_key), false));
        this.cbSavingGif.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_save_gif_key), true));
        this.cbShark.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_shake_gesture_key), false));
        this.cbVibrate.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_vibrate_key), true));
        this.mRootView.findViewById(R.id.layout_choose_app).setEnabled(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_enable_target_app_key), false));
        checkAudioRecPermission();
        if (this.cbFloatControls.isChecked()) {
            requestSystemWindowsPermission(Const.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE);
        }
        if (this.cbCamera.isChecked()) {
            requestCameraPermission();
            requestSystemWindowsPermission(Const.CAMERA_SYSTEM_WINDOWS_CODE);
        }
        updateResolution();
        updateFPS();
        updateBitRate();
        updateOrientation();
        updateAudio();
        updateFileName();
        updateNamePrefix();
        updateLocation();
        updateTimer();
        updateLanguage();
    }

    private void updateLocation() {
        Activity activity2 = getActivity();
        String string = getString(R.string.savelocation_key);
        ((TextView) this.mRootView.findViewById(R.id.value_location)).setText(PrefUtils.readStringValue(activity2, string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNamePrefix() {
        ((TextView) this.mRootView.findViewById(R.id.value_name_prefix)).setText(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFileName() {
        ((TextView) this.mRootView.findViewById(R.id.value_name_format)).setText(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX) + "_" + PrefUtils.readStringValue(getActivity(), getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudio() {
        ((TextView) this.mRootView.findViewById(R.id.value_audio)).setText(Utils.getValue(getResources().getStringArray(R.array.audioSettingsEntries), getResources().getStringArray(R.array.audioSettingsValues), PrefUtils.readStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOrientation() {
        ((TextView) this.mRootView.findViewById(R.id.value_orientation)).setText(Utils.getValue(getResources().getStringArray(R.array.orientationEntries), getResources().getStringArray(R.array.orientationValues), PrefUtils.readStringValue(getActivity(), getString(R.string.orientation_key), PrefUtils.VALUE_ORIENTATION)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBitRate() {
        ((TextView) this.mRootView.findViewById(R.id.value_bit_rate)).setText(Utils.getValue(getResources().getStringArray(R.array.bitrateArray), getResources().getStringArray(R.array.bitratesValue), PrefUtils.readStringValue(getActivity(), getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFPS() {
        ((TextView) this.mRootView.findViewById(R.id.value_frams)).setText(Utils.getValue(getResources().getStringArray(R.array.fpsArray), getResources().getStringArray(R.array.fpsArray), PrefUtils.readStringValue(getActivity(), getString(R.string.fps_key), PrefUtils.VALUE_FRAMES)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x004d  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0056  */
    private void checkAudioRecPermission() {
        char c;
        String value = Utils.getValue(getResources().getStringArray(R.array.audioSettingsEntries), getResources().getStringArray(R.array.audioSettingsValues), PrefUtils.readStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO));
        int hashCode = value.hashCode();
        if (hashCode != 49) {
            if (hashCode == 50 && value.equals(ExifInterface.GPS_MEASUREMENT_2D)) {
                c = 1;
                if (c == 0) {
                    requestAudioPermission(Const.AUDIO_REQUEST_CODE);
                } else if (c == 1) {
                    requestAudioPermission(Const.INTERNAL_AUDIO_REQUEST_CODE);
                }
                updateAudio();
            }
        } else if (value.equals(PrefUtils.VALUE_AUDIO)) {
            c = 0;
            if (c == 0) {
            }
            updateAudio();
        }
        c = 65535;
        if (c == 0) {
        }
        updateAudio();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateResolution() {
        ((TextView) this.mRootView.findViewById(R.id.value_resolution)).setText(Utils.getValue(getResources().getStringArray(R.array.resolutionsArray), getResources().getStringArray(R.array.resolutionValues), PrefUtils.readStringValue(getActivity(), getString(R.string.res_key), PrefUtils.VALUE_RESOLUTION)));
    }

    private ArrayList<String> buildEntries(int i) {
        int screenWidth = getScreenWidth(getRealDisplayMetrics());
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(getResources().getStringArray(i)));
        Iterator<String> it = arrayList.iterator();
        while (it.hasNext()) {
            if (screenWidth < Integer.parseInt(it.next())) {
                it.remove();
            }
        }
        if (!arrayList.contains("" + screenWidth)) {
            arrayList.add("" + screenWidth);
        }
        return arrayList;
    }

    private DisplayMetrics getRealDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getActivity().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private int getScreenWidth(DisplayMetrics displayMetrics) {
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight(DisplayMetrics displayMetrics) {
        return displayMetrics.heightPixels;
    }

    @Deprecated
    private Const.ASPECT_RATIO getAspectRatio() {
        float screenWidth = (float) getScreenWidth(getRealDisplayMetrics());
        float screenHeight = (float) getScreenHeight(getRealDisplayMetrics());
        return Const.ASPECT_RATIO.valueOf(screenWidth > screenHeight ? screenWidth / screenHeight : screenHeight / screenWidth);
    }

    private void setPermissionListener() {
        if (getActivity() != null && (getActivity() instanceof HomeActivity)) {
            this.activity = (HomeActivity) getActivity();
            this.activity.setPermissionResultListener(this);
        }
    }

    public void onResume() {
        if (FloatingControlService.getInstance() == null) {
            ((HomeActivity) getActivity()).startService();
        }
        super.onResume();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
    }

    private void showInternalAudioWarning(boolean z) {
        int i;
        final int i2;
        if (z) {
            i = R.string.alert_dialog_r_submix_audio_warning_message;
            i2 = Const.INTERNAL_R_SUBMIX_AUDIO_REQUEST_CODE;
        } else {
            i = R.string.alert_dialog_internal_audio_warning_message;
            i2 = Const.INTERNAL_AUDIO_REQUEST_CODE;
        }
        new AlertDialog.Builder(this.activity).setTitle(R.string.alert_dialog_internal_audio_warning_title).setMessage(i).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass10 */

            public void onClick(DialogInterface dialogInterface, int i) {
                SettingsFragment.this.requestAudioPermission(i2);
            }
        }).setNegativeButton(R.string.alert_dialog_internal_audio_warning_negative_btn_text, new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass9 */

            public void onClick(DialogInterface dialogInterface, int i) {
                SettingsFragment.this.prefs.edit().putBoolean(Const.PREFS_INTERNAL_AUDIO_DIALOG_KEY, true).apply();
                SettingsFragment.this.requestAudioPermission(Const.INTERNAL_AUDIO_REQUEST_CODE);
            }
        }).create().show();
    }

    public void requestAudioPermission(int i) {
        HomeActivity homeActivity = this.activity;
        if (homeActivity != null) {
            homeActivity.requestPermissionAudio(i);
        }
    }

    public void requestCameraPermission() {
        HomeActivity homeActivity = this.activity;
        if (homeActivity != null) {
            homeActivity.requestPermissionCamera();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestSystemWindowsPermission(int i) {
        if (this.activity == null || Build.VERSION.SDK_INT < 23) {
            getActivity().startService(new Intent(getActivity(), FloatingControlService.class));
            return;
        }
        this.activity.requestSystemWindowsPermission(i);
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this.activity).setTitle(R.string.alert_permission_denied_title).setMessage(R.string.alert_permission_denied_message).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass12 */

            public void onClick(DialogInterface dialogInterface, int i) {
                if (SettingsFragment.this.activity != null) {
                    SettingsFragment.this.activity.requestPermissionStorage();
                }
            }
        }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass11 */

            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setIconAttribute(16843605).setCancelable(false).create().show();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.testlubu.screenrecorder.interfaces.PermissionResultListener
    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        switch (i) {
            case Const.EXTDIR_REQUEST_CODE /*{ENCODED_INT: 1110}*/:
                if (iArr.length > 0 && iArr[0] == -1) {
                    Log.d(Const.TAG, "Storage permission denied. Requesting again");
                    this.mRootView.findViewById(R.id.layout_location).setEnabled(false);
                    showPermissionDeniedDialog();
                    return;
                } else if (iArr.length > 0 && iArr[0] == 0) {
                    this.mRootView.findViewById(R.id.layout_location).setEnabled(true);
                    return;
                } else {
                    return;
                }
            case Const.AUDIO_REQUEST_CODE /*{ENCODED_INT: 1111}*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(Const.TAG, "Record audio permission denied");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), "0");
                } else {
                    Log.d(Const.TAG, "Record audio permission granted.");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO);
                }
                updateAudio();
                return;
            case Const.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE /*{ENCODED_INT: 1112}*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(Const.TAG, "System Windows permission denied");
                    this.cbFloatControls.setChecked(false);
                    return;
                }
                Log.d(Const.TAG, "System Windows permission granted");
                this.cbFloatControls.setChecked(true);
                getActivity().startService(new Intent(getActivity(), FloatingControlService.class));
                return;
            case Const.CAMERA_REQUEST_CODE /*{ENCODED_INT: 1116}*/:
                if (iArr.length > 0 && iArr[0] == 0) {
                    Log.d(Const.TAG, "System Windows permission granted");
                    requestSystemWindowsPermission(Const.CAMERA_SYSTEM_WINDOWS_CODE);
                    break;
                } else {
                    Log.d(Const.TAG, "System Windows permission denied");
                    this.cbCamera.setChecked(false);
                    break;
                }
            case Const.CAMERA_SYSTEM_WINDOWS_CODE /*{ENCODED_INT: 1117}*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(Const.TAG, "System Windows permission denied");
                    this.cbCamera.setChecked(false);
                    return;
                }
                Log.d(Const.TAG, "System Windows permission granted");
                this.cbCamera.setChecked(true);
                return;
            case Const.INTERNAL_AUDIO_REQUEST_CODE /*{ENCODED_INT: 1118}*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(Const.TAG, "Record audio permission denied");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), "0");
                } else {
                    Log.d(Const.TAG, "Record audio permission granted.");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), ExifInterface.GPS_MEASUREMENT_2D);
                }
                updateAudio();
                return;
            case Const.INTERNAL_R_SUBMIX_AUDIO_REQUEST_CODE /*{ENCODED_INT: 1119}*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(Const.TAG, "Record audio permission denied");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), "0");
                } else {
                    Log.d(Const.TAG, "Record audio permission granted.");
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), "3");
                }
                updateAudio();
                return;
        }
        Log.d(Const.TAG, "Unknown permission request with request code: " + i);
    }

    @Override // com.testlubu.screenrecorder.folderpicker.OnDirectorySelectedListerner
    public void onDirectorySelected() {
        Log.d(Const.TAG, "In settings fragment");
        if (getActivity() != null && (getActivity() instanceof HomeActivity)) {
            ((HomeActivity) getActivity()).onDirectoryChanged();
        }
        updateLocation();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_audio /*{ENCODED_INT: 2131361992}*/:
                openAudioDialog();
                return;
            case R.id.layout_bit_rate /*{ENCODED_INT: 2131361993}*/:
                openBitRate();
                return;
            case R.id.layout_brush /*{ENCODED_INT: 2131361994}*/:
            case R.id.layout_close /*{ENCODED_INT: 2131361997}*/:
            case R.id.layout_surface_view /*{ENCODED_INT: 2131362009}*/:
            case R.id.layout_time /*{ENCODED_INT: 2131362010}*/:
            default:
                return;
            case R.id.layout_camera_overlay /*{ENCODED_INT: 2131361995}*/:
                SwitchCompat switchCompat = this.cbCamera;
                switchCompat.setChecked(!switchCompat.isChecked());
                return;
            case R.id.layout_choose_app /*{ENCODED_INT: 2131361996}*/:
                this.appPickerDialog = new AppPickerDialog(getActivity());
                this.appPickerDialog.show();
                return;
            case R.id.layout_enable_saving_in_gif /*{ENCODED_INT: 2131361998}*/:
                SwitchCompat switchCompat2 = this.cbSavingGif;
                switchCompat2.setChecked(!switchCompat2.isChecked());
                return;
            case R.id.layout_enable_target_app /*{ENCODED_INT: 2131361999}*/:
                SwitchCompat switchCompat3 = this.cbTargetApp;
                switchCompat3.setChecked(!switchCompat3.isChecked());
                return;
            case R.id.layout_frams /*{ENCODED_INT: 2131362000}*/:
                openFramesDialog();
                return;
            case R.id.layout_language /*{ENCODED_INT: 2131362001}*/:
                openLanguage();
                return;
            case R.id.layout_location /*{ENCODED_INT: 2131362002}*/:
                this.folderChooserDialog.show();
                return;
            case R.id.layout_name_format /*{ENCODED_INT: 2131362003}*/:
                openNameFormat();
                return;
            case R.id.layout_name_prefix /*{ENCODED_INT: 2131362004}*/:
                openNamePrefix();
                return;
            case R.id.layout_orientation /*{ENCODED_INT: 2131362005}*/:
                openOrientationDialog();
                return;
            case R.id.layout_resolution /*{ENCODED_INT: 2131362006}*/:
                openResolutionDialog();
                return;
            case R.id.layout_shark /*{ENCODED_INT: 2131362007}*/:
                SwitchCompat switchCompat4 = this.cbShark;
                switchCompat4.setChecked(!switchCompat4.isChecked());
                return;
            case R.id.layout_show_touches /*{ENCODED_INT: 2131362008}*/:
                startActivity(new Intent(getActivity(), ShowTouchTutsActivity.class));
                return;
            case R.id.layout_timer /*{ENCODED_INT: 2131362011}*/:
                openTimer();
                return;
            case R.id.layout_use_float_controls /*{ENCODED_INT: 2131362012}*/:
                SwitchCompat switchCompat5 = this.cbFloatControls;
                switchCompat5.setChecked(!switchCompat5.isChecked());
                return;
            case R.id.layout_vibrate /*{ENCODED_INT: 2131362013}*/:
                SwitchCompat switchCompat6 = this.cbVibrate;
                switchCompat6.setChecked(!switchCompat6.isChecked());
                return;
        }
    }

    private void openTimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_timer_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.timerArray), Utils.getPosition(getResources().getStringArray(R.array.timer), PrefUtils.readStringValue(getActivity(), getString(R.string.timer_key), "3")), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass13 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.timer_key), SettingsFragment.this.getResources().getStringArray(R.array.timer)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass14 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass15 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateTimer();
            }
        });
        create.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTimer() {
        ((TextView) this.mRootView.findViewById(R.id.value_timer)).setText(Utils.getValue(getResources().getStringArray(R.array.timerArray), getResources().getStringArray(R.array.timer), PrefUtils.readStringValue(getActivity(), getString(R.string.timer_key), "3")));
    }

    private void openLanguage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_language_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.language), Utils.getPosition(getResources().getStringArray(R.array.languageValue), PrefUtils.readStringValue(getActivity(), getString(R.string.language_key), PrefUtils.VALUE_LANGUAGE)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass16 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.language_key), SettingsFragment.this.getResources().getStringArray(R.array.languageValue)[i]);
                SettingsFragment settingsFragment = SettingsFragment.this;
                settingsFragment.setLocale(settingsFragment.getResources().getStringArray(R.array.languageValue)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass17 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass18 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateLanguage();
            }
        });
        create.show();
    }

    public void setLocale(String str) {
        Locale locale = new Locale(str);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
        startActivity(new Intent(getActivity(), HomeActivity.class));
        getActivity().finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLanguage() {
        ((TextView) this.mRootView.findViewById(R.id.value_language)).setText(Utils.getValue(getResources().getStringArray(R.array.language), getResources().getStringArray(R.array.languageValue), PrefUtils.readStringValue(getActivity(), getString(R.string.language_key), PrefUtils.VALUE_LANGUAGE)));
    }

    private void openNamePrefix() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.preference_filename_prefix_title));
        final EditText editText = new EditText(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        int convertDpToPixel = Utils.convertDpToPixel(20.0f, getActivity());
        layoutParams.setMargins(convertDpToPixel, convertDpToPixel, convertDpToPixel, convertDpToPixel);
        editText.setLayoutParams(layoutParams);
        editText.setText(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX));
        editText.setSelection(editText.getText().toString().length());
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass19 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.fileprefix_key), editText.getText().toString());
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass20 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass21 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateNamePrefix();
            }
        });
        create.show();
    }

    private void openNameFormat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_filename_format_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.filename), Utils.getPosition(getResources().getStringArray(R.array.filename), PrefUtils.readStringValue(getActivity(), getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass22 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.filename_key), SettingsFragment.this.getResources().getStringArray(R.array.filename)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass23 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass24 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateFileName();
            }
        });
        create.show();
    }

    private void openAudioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_audio_record_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.audioSettingsEntries), Utils.getPosition(getResources().getStringArray(R.array.audioSettingsValues), PrefUtils.readStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass25 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.audiorec_key), SettingsFragment.this.getResources().getStringArray(R.array.audioSettingsValues)[i]);
                SettingsFragment.this.checkAudioRecPermission();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass26 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass27 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateAudio();
            }
        });
        create.show();
    }

    private void openOrientationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_orientation_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.orientationEntries), Utils.getPosition(getResources().getStringArray(R.array.orientationValues), PrefUtils.readStringValue(getActivity(), getString(R.string.orientation_key), PrefUtils.VALUE_ORIENTATION)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass28 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.orientation_key), SettingsFragment.this.getResources().getStringArray(R.array.orientationValues)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass29 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass30 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateOrientation();
            }
        });
        create.show();
    }

    private void openBitRate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_bit_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.bitrateArray), Utils.getPosition(getResources().getStringArray(R.array.bitratesValue), PrefUtils.readStringValue(getActivity(), getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass31 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.bitrate_key), SettingsFragment.this.getResources().getStringArray(R.array.bitratesValue)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass32 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass33 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateBitRate();
            }
        });
        create.show();
    }

    private void openFramesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_fps_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.fpsArray), Utils.getPosition(getResources().getStringArray(R.array.fpsArray), PrefUtils.readStringValue(getActivity(), getString(R.string.fps_key), PrefUtils.VALUE_FRAMES)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass34 */

            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.fps_key), SettingsFragment.this.getResources().getStringArray(R.array.fpsArray)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass35 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass36 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateFPS();
            }
        });
        create.show();
    }

    private void openResolutionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.preference_resolution_title);
        final String[] stringArray = getResources().getStringArray(R.array.resolutionsArray);
        builder.setSingleChoiceItems(stringArray, Utils.getPosition(getResources().getStringArray(R.array.resolutionValues), PrefUtils.readStringValue(getActivity(), getString(R.string.res_key), PrefUtils.VALUE_RESOLUTION)), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass37 */

            public void onClick(DialogInterface dialogInterface, int i) {
                if (Integer.parseInt(SettingsFragment.this.getNativeRes()) < Integer.parseInt(SettingsFragment.this.getResources().getStringArray(R.array.resolutionValues)[i])) {
                    Activity activity = SettingsFragment.this.getActivity();
                    Toast.makeText(activity, SettingsFragment.this.getString(R.string.notsupport) + stringArray[i], 1).show();
                    return;
                }
                PrefUtils.saveStringValue(SettingsFragment.this.getActivity(), SettingsFragment.this.getString(R.string.res_key), SettingsFragment.this.getResources().getStringArray(R.array.resolutionValues)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass38 */

            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.SettingsFragment.AnonymousClass39 */

            public void onDismiss(DialogInterface dialogInterface) {
                SettingsFragment.this.updateResolution();
            }
        });
        create.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getNativeRes() {
        return String.valueOf(getScreenWidth(getRealDisplayMetrics()));
    }
}
