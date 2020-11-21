package com.testlubu.screenrecorder.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;

@TargetApi(24)
public class QuickRecordTile extends TileService {
    private boolean isTileActive;

    public void onStartListening() {
        super.onStartListening();
        this.isTileActive = isServiceRunning(RecorderService.class);
        changeTileState();
    }

    public void onClick() {
        this.isTileActive = getQsTile().getState() != 2;
        changeTileState();
        if (this.isTileActive) {
            startActivity(new Intent(this, HomeActivity.class).setAction(getString(R.string.app_shortcut_action)).setFlags(268435456));
        } else {
            startService(new Intent(this, RecorderService.class).setAction(Const.SCREEN_RECORDING_STOP));
        }
        this.isTileActive = !this.isTileActive;
    }

    private void changeTileState() {
        Tile qsTile = super.getQsTile();
        int i = this.isTileActive ? 2 : 1;
        if (!this.isTileActive) {
            qsTile.setLabel(getString(R.string.quick_settings_tile_start_title));
        } else {
            qsTile.setLabel(getString(R.string.quick_settings_tile_stop_title));
        }
        qsTile.setState(i);
        qsTile.updateTile();
    }

    private boolean isServiceRunning(Class<?> cls) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
