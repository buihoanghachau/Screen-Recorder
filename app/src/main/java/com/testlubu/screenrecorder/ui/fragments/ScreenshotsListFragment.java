package com.testlubu.screenrecorder.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter;
import com.testlubu.screenrecorder.adapter.decoration.SpacesItemDecoration;
import com.testlubu.screenrecorder.common.Cache;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.interfaces.PermissionResultListener;
import com.testlubu.screenrecorder.model.Photo;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class ScreenshotsListFragment extends BaseFragment implements PermissionResultListener, SwipeRefreshLayout.OnRefreshListener {
    private PhotoRecyclerAdapter mAdapter;
    BroadcastReceiver mReceiverUpdate = new BroadcastReceiver() {
        /* class com.testlubu.screenrecorder.ui.fragments.ScreenshotsListFragment.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (Const.UPDATE_UI_IMAGE.equals(intent.getAction())) {
                ScreenshotsListFragment.this.onRefresh();
            }
        }
    };
    private TextView message;
    private RecyclerView photoRV;
    private ArrayList<Photo> photosList = new ArrayList<>();
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefreshLayout;

    private void initEvents() {
    }

    public static ScreenshotsListFragment newInstance() {
        return new ScreenshotsListFragment();
    }

    /* access modifiers changed from: private */
    public static boolean isPhotoFile(String str) {
        String guessContentTypeFromName = URLConnection.guessContentTypeFromName(str);
        return guessContentTypeFromName != null && guessContentTypeFromName.startsWith("image");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.fragment_photos, viewGroup, false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.UPDATE_UI_IMAGE);
        getActivity().registerReceiver(this.mReceiverUpdate, intentFilter);
        initViews();
        initEvents();
        return this.mRootView;
    }

    public void setEnableSwipe(boolean z) {
        this.swipeRefreshLayout.setEnabled(z);
    }

    @Override // com.testlubu.screenrecorder.ui.fragments.BaseFragment
    public void onVisibleFragment() {
        super.onVisibleFragment();
        setRecyclerView(Cache.getInstance().getArrPhotos());
        if (getActivity() != null) {
            this.photosList.clear();
            checkPermission();
        }
    }

    private void initViews() {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.message = (TextView) this.mRootView.findViewById(R.id.message_tv);
        this.photoRV = (RecyclerView) this.mRootView.findViewById(R.id.videos_rv);
        this.photoRV.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(10.0f, getActivity())));
        this.swipeRefreshLayout = (SwipeRefreshLayout) this.mRootView.findViewById(R.id.swipeRefresh);
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, 17170453, 17170457, 17170451);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        MenuItem add = menu.add("Refresh");
        add.setIcon(R.drawable.ic_refresh_white_24dp);
        add.setShowAsActionFlags(2);
        add.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.ScreenshotsListFragment.AnonymousClass2 */

            public boolean onMenuItemClick(MenuItem menuItem) {
                if (ScreenshotsListFragment.this.swipeRefreshLayout.isRefreshing()) {
                    return false;
                }
                ScreenshotsListFragment.this.photosList.clear();
                ScreenshotsListFragment.this.checkPermission();
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).setPermissionResultListener(this);
                ((HomeActivity) getActivity()).requestPermissionStorage();
            }
        } else if (this.photosList.isEmpty()) {
            SharedPreferences sharedPreferences = this.prefs;
            String string = getString(R.string.savelocation_key);
            File file = new File(sharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
            if (!file.exists()) {
                Utils.createDir();
                Log.d(Const.TAG, "Directory missing! Creating dir");
            }
            ArrayList arrayList = new ArrayList();
            if (file.isDirectory() && file.exists()) {
                arrayList.addAll(Arrays.asList(getPhotos(file.listFiles())));
            }
            new GetPhotosAsync().execute((File[]) arrayList.toArray(new File[arrayList.size()]));
        }
    }

    private File[] getPhotos(File[] fileArr) {
        ArrayList arrayList = new ArrayList();
        for (File file : fileArr) {
            if (!file.isDirectory() && isPhotoFile(file.getPath())) {
                arrayList.add(file);
            }
        }
        return (File[]) arrayList.toArray(new File[arrayList.size()]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRecyclerView(ArrayList<Photo> arrayList) {
        if (!arrayList.isEmpty() && this.message.getVisibility() != 8) {
            this.message.setVisibility(8);
        }
        this.photoRV.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        this.photoRV.setLayoutManager(gridLayoutManager);
        this.mAdapter = new PhotoRecyclerAdapter(getActivity(), arrayList, this);
        this.photoRV.setAdapter(this.mAdapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            /* class com.testlubu.screenrecorder.ui.fragments.ScreenshotsListFragment.AnonymousClass3 */

            @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
            public int getSpanSize(int i) {
                if (ScreenshotsListFragment.this.mAdapter.isSection(i)) {
                    return gridLayoutManager.getSpanCount();
                }
                return 1;
            }
        });
    }

    public void onDetach() {
        super.onDetach();
        if (this.mAdapter != null) {
            Cache.getInstance().setArrPhotos(this.mAdapter.getPhotos());
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mAdapter != null) {
            Cache.getInstance().setArrPhotos(this.mAdapter.getPhotos());
        }
    }

    @Override // com.testlubu.screenrecorder.interfaces.PermissionResultListener
    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        if (i == 1110) {
            if (iArr.length <= 0 || iArr[0] != 0) {
                Log.d(Const.TAG, "Storage permission denied.");
                this.photoRV.setVisibility(8);
                this.message.setText(R.string.video_list_permission_denied_message);
                return;
            }
            Log.d(Const.TAG, "Storage permission granted.");
            checkPermission();
        }
    }

    public void removePhotosList() {
        this.photosList.clear();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        removePhotosList();
        checkPermission();
    }

    @Override // androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
    public void onRefresh() {
        this.photosList.clear();
        checkPermission();
    }

    /* access modifiers changed from: package-private */
    public class GetPhotosAsync extends AsyncTask<File[], Integer, ArrayList<Photo>> {
        File[] files;
        ContentResolver resolver;

        GetPhotosAsync() {
            this.resolver = ScreenshotsListFragment.this.getActivity().getApplicationContext().getContentResolver();
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            ScreenshotsListFragment.this.swipeRefreshLayout.setRefreshing(true);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ArrayList<Photo> arrayList) {
            if (arrayList.isEmpty()) {
                ScreenshotsListFragment.this.photoRV.setVisibility(8);
                ScreenshotsListFragment.this.message.setVisibility(0);
            } else {
                Collections.sort(arrayList, Collections.reverseOrder());
                ScreenshotsListFragment.this.setRecyclerView(addSections(arrayList));
                ScreenshotsListFragment.this.photoRV.setVisibility(0);
                ScreenshotsListFragment.this.message.setVisibility(8);
            }
            ScreenshotsListFragment.this.swipeRefreshLayout.setRefreshing(false);
        }

        private ArrayList<Photo> addSections(ArrayList<Photo> arrayList) {
            ArrayList<Photo> arrayList2 = new ArrayList<>();
            Date date = new Date();
            for (int i = 0; i < arrayList.size(); i++) {
                Photo photo = arrayList.get(i);
                if (i == 0) {
                    arrayList2.add(new Photo(true, photo.getLastModified()));
                    arrayList2.add(photo);
                    date = photo.getLastModified();
                } else {
                    if (addNewSection(date, photo.getLastModified())) {
                        arrayList2.add(new Photo(true, photo.getLastModified()));
                        date = photo.getLastModified();
                    }
                    arrayList2.add(photo);
                }
            }
            return arrayList2;
        }

        private boolean addNewSection(Date date, Date date2) {
            return ((int) Math.abs((Utils.toCalendar(date2.getTime()).getTimeInMillis() - Utils.toCalendar(date.getTime()).getTimeInMillis()) / 86400000)) > 0;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            Log.d(Const.TAG, "Progress is :" + numArr[0]);
        }

        /* access modifiers changed from: protected */
        public ArrayList<Photo> doInBackground(File[]... fileArr) {
            this.files = fileArr[0];
            int i = 0;
            while (true) {
                File[] fileArr2 = this.files;
                if (i >= fileArr2.length) {
                    return ScreenshotsListFragment.this.photosList;
                }
                File file = fileArr2[i];
                if (!file.isDirectory() && ScreenshotsListFragment.isPhotoFile(file.getPath())) {
                    ScreenshotsListFragment.this.photosList.add(new Photo(file.getName(), file, getBitmap(file), new Date(file.lastModified())));
                    publishProgress(Integer.valueOf(i));
                }
                i++;
            }
        }

        /* access modifiers changed from: package-private */
        public Bitmap getBitmap(File file) {
            Cursor query = this.resolver.query(MediaStore.Images.Media.getContentUri("external"), new String[]{"_id", "bucket_id", "bucket_display_name", "_data"}, "_data=? ", new String[]{file.getPath()}, null);
            if (query == null || !query.moveToNext()) {
                return null;
            }
            Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(this.resolver, (long) query.getInt(query.getColumnIndexOrThrow("_id")), 1, null);
            query.close();
            return thumbnail;
        }
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mReceiverUpdate);
        super.onDestroy();
    }
}
