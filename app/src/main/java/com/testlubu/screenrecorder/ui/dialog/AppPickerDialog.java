package com.testlubu.screenrecorder.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.adapter.Apps;
import com.testlubu.screenrecorder.adapter.AppsListFragmentAdapter;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import java.util.ArrayList;
import java.util.Collections;

public class AppPickerDialog extends Dialog implements AppsListFragmentAdapter.OnItemClicked {
    private ArrayList<Apps> apps;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public AppPickerDialog(@NonNull Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            setContentView(R.layout.layout_apps_list_preference);
            this.progressBar = (ProgressBar) findViewById(R.id.appsProgressBar);
            this.recyclerView = (RecyclerView) findViewById(R.id.appsRecyclerView);
            init();
        } catch (Exception unused) {
            cancel();
        }
    }

    private void init() {
        try {
            findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.ui.dialog.AppPickerDialog.AnonymousClass1 */

                public void onClick(View view) {
                    AppPickerDialog.this.dismiss();
                }
            });
            this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            new GetApps().execute(new Void[0]);
        } catch (Exception unused) {
        }
    }

    @Override // com.testlubu.screenrecorder.adapter.AppsListFragmentAdapter.OnItemClicked
    public void onItemClick(int i) {
        Log.d(Const.TAG, "Closing dialog. received result. Pos:" + i);
        PrefUtils.saveStringValue(getContext(), getContext().getString(R.string.preference_app_chooser_key), this.apps.get(i).getPackageName());
        dismiss();
    }

    /* access modifiers changed from: package-private */
    public class GetApps extends AsyncTask<Void, Void, ArrayList<Apps>> {
        GetApps() {
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ArrayList<Apps> arrayList) {
            super.onPostExecute(arrayList);
            try {
                AppPickerDialog.this.progressBar.setVisibility(8);
                AppsListFragmentAdapter appsListFragmentAdapter = new AppsListFragmentAdapter(arrayList);
                AppPickerDialog.this.recyclerView.setAdapter(appsListFragmentAdapter);
                appsListFragmentAdapter.setOnClick(AppPickerDialog.this);
            } catch (Exception unused) {
            }
        }

        /* access modifiers changed from: protected */
        public ArrayList<Apps> doInBackground(Void... voidArr) {
            try {
                PackageManager packageManager = AppPickerDialog.this.getContext().getPackageManager();
                AppPickerDialog.this.apps = new ArrayList();
                for (PackageInfo packageInfo : packageManager.getInstalledPackages(0)) {
                    if (!AppPickerDialog.this.getContext().getPackageName().equals(packageInfo.packageName) && packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        Apps apps = new Apps(packageInfo.applicationInfo.loadLabel(AppPickerDialog.this.getContext().getPackageManager()).toString(), packageInfo.packageName, packageInfo.applicationInfo.loadIcon(AppPickerDialog.this.getContext().getPackageManager()));
                        apps.setSelectedApp(PrefUtils.readStringValue(AppPickerDialog.this.getContext(), AppPickerDialog.this.getContext().getString(R.string.preference_app_chooser_key), "none").equals(packageInfo.packageName));
                        if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) == null) {
                            Log.d(Const.TAG, packageInfo.packageName);
                        }
                        AppPickerDialog.this.apps.add(apps);
                    }
                    Collections.sort(AppPickerDialog.this.apps);
                }
                return AppPickerDialog.this.apps;
            } catch (Exception unused) {
                return null;
            }
        }
    }
}
