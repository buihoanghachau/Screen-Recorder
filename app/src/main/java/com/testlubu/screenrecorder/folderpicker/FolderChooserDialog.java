package com.testlubu.screenrecorder.folderpicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.PrefUtils;
import com.testlubu.screenrecorder.folderpicker.DirectoryRecyclerAdapter;
import com.testlubu.screenrecorder.folderpicker.Storages;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderChooserDialog extends Dialog implements View.OnClickListener, DirectoryRecyclerAdapter.OnDirectoryClickedListerner, AdapterView.OnItemSelectedListener {
    public static OnDirectorySelectedListerner onDirectorySelectedListerner;
    private DirectoryRecyclerAdapter adapter;
    public File currentDir;
    private AlertDialog dialog;
    private ArrayList<File> directories;
    private boolean isExternalStorageSelected = false;
    private SharedPreferences prefs;
    private RecyclerView rv;
    private Spinner spinner;
    private List<Storages> storages = new ArrayList();
    private TextView tv_currentDir;
    private TextView tv_empty;

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public FolderChooserDialog(@NonNull Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initialize();
    }

    private void initialize() {
        try {
            setContentView(R.layout.director_chooser);
            this.currentDir = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
            ContextCompat.getExternalFilesDirs(getContext().getApplicationContext(), null);
            this.storages.add(new Storages(Environment.getExternalStorageDirectory().getPath(), Storages.StorageType.Internal));
            this.prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            generateFoldersList();
            initView();
            initRecyclerView();
        } catch (Exception unused) {
        }
    }

    private void initRecyclerView() {
        try {
            this.rv.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), 1, false);
            this.rv.setLayoutManager(linearLayoutManager);
            this.rv.addItemDecoration(new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation()));
            if (!isDirectoryEmpty()) {
                this.adapter = new DirectoryRecyclerAdapter(getContext(), this, this.directories);
                this.rv.setAdapter(this.adapter);
            }
            this.tv_currentDir.setText(this.currentDir.getPath());
        } catch (Exception unused) {
        }
    }

    private boolean isDirectoryEmpty() {
        if (this.directories.isEmpty()) {
            this.rv.setVisibility(8);
            this.tv_empty.setVisibility(0);
            return true;
        }
        this.rv.setVisibility(0);
        this.tv_empty.setVisibility(8);
        return false;
    }

    private void generateFoldersList() {
        try {
            this.directories = new ArrayList<>(Arrays.asList(this.currentDir.listFiles(new DirectoryFilter())));
            Collections.sort(this.directories, new SortFileName());
            Log.d(Const.TAG, "Directory size " + this.directories.size());
        } catch (Exception unused) {
        }
    }

    private void initView() {
        try {
            ((Button) findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass1 */

                public void onClick(View view) {
                    if (!FolderChooserDialog.this.currentDir.canWrite()) {
                        Toast.makeText(FolderChooserDialog.this.getContext(), "Cannot write to selected directory. Path will not be saved.", 0).show();
                        return;
                    }
                    PrefUtils.saveStringValue(FolderChooserDialog.this.getContext(), FolderChooserDialog.this.getContext().getString(R.string.savelocation_key), FolderChooserDialog.this.currentDir.getPath());
                    FolderChooserDialog.onDirectorySelectedListerner.onDirectorySelected();
                    FolderChooserDialog.this.dismiss();
                }
            });
            ((Button) findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass2 */

                public void onClick(View view) {
                    FolderChooserDialog.this.dismiss();
                }
            });
            this.tv_currentDir = (TextView) findViewById(R.id.tv_selected_dir);
            this.rv = (RecyclerView) findViewById(R.id.rv);
            this.tv_empty = (TextView) findViewById(R.id.tv_empty);
            this.spinner = (Spinner) findViewById(R.id.storageSpinner);
            ((ImageButton) findViewById(R.id.nav_up)).setOnClickListener(this);
            ((ImageButton) findViewById(R.id.create_dir)).setOnClickListener(this);
            ArrayList arrayList = new ArrayList();
            for (Storages storages2 : this.storages) {
                arrayList.add(storages2.getType() == Storages.StorageType.Internal ? "Internal Storage" : "Removable Storage");
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), 17367048, arrayList);
            arrayAdapter.setDropDownViewResource(17367049);
            this.spinner.setAdapter((SpinnerAdapter) arrayAdapter);
            this.spinner.setOnItemSelectedListener(this);
        } catch (Exception unused) {
        }
    }

    private void changeDirectory(File file) {
        try {
            this.currentDir = file;
            Log.d(Const.TAG, "Changed dir is: " + file.getPath());
            generateFoldersList();
            if (!isDirectoryEmpty()) {
                this.adapter = new DirectoryRecyclerAdapter(getContext(), this, this.directories);
                this.rv.swapAdapter(this.adapter, true);
            }
            this.tv_currentDir.setText(this.currentDir.getPath());
        } catch (Exception unused) {
        }
    }

    public void setCurrentDir(String str) {
        try {
            File file = new File(str);
            if (!file.exists() || !file.isDirectory()) {
                createFolder(file.getPath());
                Log.d(Const.TAG, "Directory created");
                return;
            }
            this.currentDir = file;
            Log.d(Const.TAG, "Directory set");
        } catch (Exception unused) {
        }
    }

    public void setOnDirectoryClickedListerner(OnDirectorySelectedListerner onDirectorySelectedListerner2) {
        onDirectorySelectedListerner = onDirectorySelectedListerner2;
    }

    private void newDirDialog(Bundle bundle) {
        try {
            View inflate = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.directory_chooser_edit_text, (ViewGroup) null);
            final EditText editText = (EditText) inflate.findViewById(R.id.et_new_folder);
            editText.addTextChangedListener(new TextWatcher() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass3 */

                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void afterTextChanged(Editable editable) {
                    if (FolderChooserDialog.this.dialog != null) {
                        FolderChooserDialog.this.dialog.getButton(-1).setEnabled(!editable.toString().trim().isEmpty());
                    }
                }
            });
            this.dialog = new AlertDialog.Builder(getContext()).setTitle(R.string.alert_title_create_folder).setMessage(R.string.alert_message_create_folder).setView(inflate).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass5 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass4 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    String trim = editText.getText().toString().trim();
                    if (!trim.isEmpty()) {
                        FolderChooserDialog.this.createFolder(trim);
                    }
                }
            }).create();
            if (bundle != null) {
                this.dialog.onRestoreInstanceState(bundle);
            }
            this.dialog.show();
            this.dialog.getButton(-1).setEnabled(!editText.getText().toString().trim().isEmpty());
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean createFolder(String str) {
        File file;
        try {
            if (this.currentDir == null) {
                Toast.makeText(getContext(), "No directory selected", 0).show();
                return false;
            } else if (!this.currentDir.canWrite()) {
                Toast.makeText(getContext(), getContext().getString(R.string.error_permission_make_dir), 0).show();
                return false;
            } else {
                if (str.contains(Environment.getExternalStorageDirectory().getPath())) {
                    file = new File(str);
                } else {
                    file = new File(this.currentDir, str);
                }
                if (file.exists()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.dir_exist), 0).show();
                    changeDirectory(new File(this.currentDir, str));
                    return false;
                } else if (!file.mkdir()) {
                    Toast.makeText(getContext(), "Error creating directory", 0).show();
                    Log.d(Const.TAG, file.getPath());
                    return false;
                } else {
                    changeDirectory(new File(this.currentDir, str));
                    return true;
                }
            }
        } catch (Exception unused) {
            return true;
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.create_dir) {
            newDirDialog(null);
        } else if (id == R.id.nav_up) {
            try {
                File file = new File(this.currentDir.getParent());
                Log.d(Const.TAG, file.getPath());
                if (this.isExternalStorageSelected) {
                    changeExternalDirectory(file);
                } else if (file.getPath().contains(this.storages.get(0).getPath())) {
                    changeDirectory(file);
                }
            } catch (Exception unused) {
            }
        }
    }

    private void changeExternalDirectory(File file) {
        try {
            String removableSDPath = getRemovableSDPath(this.storages.get(1).getPath());
            if (file.getPath().contains(removableSDPath) && file.canWrite()) {
                changeDirectory(file);
            } else if (file.getPath().contains(removableSDPath) && !file.canWrite()) {
                Toast.makeText(getContext(), (int) R.string.external_storage_dir_not_writable, 0).show();
            }
        } catch (Exception unused) {
        }
    }

    private String getRemovableSDPath(String str) {
        int indexOf = str.indexOf("Android");
        Log.d(Const.TAG, "Short code is: " + str.substring(0, indexOf));
        String substring = str.substring(0, indexOf + -1);
        Log.d(Const.TAG, "External Base Dir " + substring);
        return substring;
    }

    @Override // com.testlubu.screenrecorder.folderpicker.DirectoryRecyclerAdapter.OnDirectoryClickedListerner
    public void OnDirectoryClicked(File file) {
        changeDirectory(file);
    }

    @Override // android.widget.AdapterView.OnItemSelectedListener
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        Log.d(Const.TAG, "Selected storage is: " + this.storages.get(i));
        this.isExternalStorageSelected = this.storages.get(i).getType() == Storages.StorageType.External;
        if (this.isExternalStorageSelected && !this.prefs.getBoolean(Const.ALERT_EXTR_STORAGE_CB_KEY, false)) {
            showExtDirAlert();
        }
        changeDirectory(new File(this.storages.get(i).getPath()));
    }

    private void showExtDirAlert() {
        try {
            View inflate = View.inflate(getContext(), R.layout.alert_checkbox, null);
            final CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.donot_warn_cb);
            new AlertDialog.Builder(getContext()).setTitle(R.string.alert_ext_dir_warning_title).setMessage(R.string.alert_ext_dir_warning_message).setView(inflate).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.folderpicker.FolderChooserDialog.AnonymousClass6 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (checkBox.isChecked()) {
                            FolderChooserDialog.this.prefs.edit().putBoolean(Const.ALERT_EXTR_STORAGE_CB_KEY, true).apply();
                        }
                    } catch (Exception unused) {
                    }
                }
            }).create().show();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    public class DirectoryFilter implements FileFilter {
        private DirectoryFilter() {
        }

        public boolean accept(File file) {
            return file.isDirectory() && !file.isHidden();
        }
    }

    /* access modifiers changed from: private */
    public class SortFileName implements Comparator<File> {
        private SortFileName() {
        }

        public int compare(File file, File file2) {
            return file.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
        }
    }
}
