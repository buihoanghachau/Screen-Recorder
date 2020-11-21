package com.testlubu.screenrecorder.ui.fragments;

import android.content.ContentResolver;
import android.content.Intent;
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
import com.testlubu.screenrecorder.adapter.VideoEditedRecyclerAdapter;
import com.testlubu.screenrecorder.adapter.decoration.SpacesItemDecoration;
import com.testlubu.screenrecorder.common.Cache;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.interfaces.PermissionResultListener;
import com.testlubu.screenrecorder.model.Video;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class VideosEditedListFragment extends BaseFragment implements PermissionResultListener, SwipeRefreshLayout.OnRefreshListener {
    private VideoEditedRecyclerAdapter mAdapter;
    private TextView message;
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView videoRV;
    private ArrayList<Video> videosList = new ArrayList<>();

    public static VideosEditedListFragment newInstance() {
        return new VideosEditedListFragment();
    }

    /* access modifiers changed from: private */
    public static boolean isVideoFile(String str) {
        String guessContentTypeFromName = URLConnection.guessContentTypeFromName(str);
        return guessContentTypeFromName != null && guessContentTypeFromName.startsWith("video");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_videos, viewGroup, false);
        try {
            this.message = (TextView) inflate.findViewById(R.id.message_tv);
            this.videoRV = (RecyclerView) inflate.findViewById(R.id.videos_rv);
            this.videoRV.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(10.0f, getActivity())));
            this.swipeRefreshLayout = (SwipeRefreshLayout) inflate.findViewById(R.id.swipeRefresh);
            this.swipeRefreshLayout.setOnRefreshListener(this);
            this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, 17170453, 17170457, 17170451);
            this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        } catch (Exception unused) {
        }
        return inflate;
    }

    @Override // com.testlubu.screenrecorder.ui.fragments.BaseFragment
    public void onVisibleFragment() {
        super.onVisibleFragment();
        setRecyclerView(Cache.getInstance().getArrVideosEdited());
        if (getActivity() != null) {
            this.videosList.clear();
            checkPermission();
        }
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
            /* class com.testlubu.screenrecorder.ui.fragments.VideosEditedListFragment.AnonymousClass1 */

            public boolean onMenuItemClick(MenuItem menuItem) {
                if (VideosEditedListFragment.this.swipeRefreshLayout.isRefreshing()) {
                    return false;
                }
                VideosEditedListFragment.this.videosList.clear();
                VideosEditedListFragment.this.checkPermission();
                Log.d(Const.TAG, "Refreshing");
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPermission() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setPermissionResultListener(this);
                    ((HomeActivity) getActivity()).requestPermissionStorage();
                }
            } else if (this.videosList.isEmpty()) {
                SharedPreferences sharedPreferences = this.prefs;
                String string = getString(R.string.savelocation_key);
                File file = new File(sharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
                if (!file.exists()) {
                    Utils.createDirEdited();
                    Log.d(Const.TAG, "Directory missing! Creating dir");
                }
                ArrayList arrayList = new ArrayList();
                if (file.isDirectory() && file.exists()) {
                    arrayList.addAll(Arrays.asList(getVideos(file.listFiles())));
                }
                new GetVideosAsync().execute((File[]) arrayList.toArray(new File[arrayList.size()]));
            }
        } catch (Exception unused) {
        }
    }

    private File[] getVideos(File[] fileArr) {
        try {
            ArrayList arrayList = new ArrayList();
            for (File file : fileArr) {
                if (!file.isDirectory() && isVideoFile(file.getPath())) {
                    arrayList.add(file);
                }
            }
            return (File[]) arrayList.toArray(new File[arrayList.size()]);
        } catch (Exception unused) {
            return null;
        }
    }

    public void onDetach() {
        super.onDetach();
        if (this.mAdapter != null) {
            Cache.getInstance().setArrVideosEdited(this.mAdapter.getVideos());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRecyclerView(ArrayList<Video> arrayList) {
        try {
            if (!arrayList.isEmpty() && this.message.getVisibility() != 8) {
                this.message.setVisibility(8);
            }
            this.videoRV.setHasFixedSize(true);
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            this.videoRV.setLayoutManager(gridLayoutManager);
            this.mAdapter = new VideoEditedRecyclerAdapter(getActivity(), arrayList, this);
            this.videoRV.setAdapter(this.mAdapter);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                /* class com.testlubu.screenrecorder.ui.fragments.VideosEditedListFragment.AnonymousClass2 */

                @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
                public int getSpanSize(int i) {
                    if (VideosEditedListFragment.this.mAdapter.isSection(i)) {
                        return gridLayoutManager.getSpanCount();
                    }
                    return 1;
                }
            });
        } catch (Exception unused) {
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mAdapter != null) {
            Cache.getInstance().setArrVideos(this.mAdapter.getVideos());
        }
    }

    @Override // com.testlubu.screenrecorder.interfaces.PermissionResultListener
    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        if (i == 1110) {
            if (iArr.length <= 0 || iArr[0] != 0) {
                Log.d(Const.TAG, "Storage permission denied.");
                this.videoRV.setVisibility(8);
                this.message.setText(R.string.video_list_permission_denied_message);
                return;
            }
            Log.d(Const.TAG, "Storage permission granted.");
            checkPermission();
        }
    }

    public void removeVideosList() {
        this.videosList.clear();
        Log.d(Const.TAG, "Reached video fragment");
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        removeVideosList();
        checkPermission();
    }

    @Override // androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
    public void onRefresh() {
        this.videosList.clear();
        checkPermission();
    }

    public void setEnableSwipe(boolean z) {
        this.swipeRefreshLayout.setEnabled(z);
    }

    /* access modifiers changed from: package-private */
    public class GetVideosAsync extends AsyncTask<File[], Integer, ArrayList<Video>> {
        File[] files;
        ContentResolver resolver;

        GetVideosAsync() {
            this.resolver = VideosEditedListFragment.this.getActivity().getApplicationContext().getContentResolver();
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            VideosEditedListFragment.this.swipeRefreshLayout.setRefreshing(true);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ArrayList<Video> arrayList) {
            try {
                if (arrayList.isEmpty()) {
                    VideosEditedListFragment.this.videoRV.setVisibility(8);
                    VideosEditedListFragment.this.message.setVisibility(0);
                } else {
                    Collections.sort(arrayList, Collections.reverseOrder());
                    VideosEditedListFragment.this.setRecyclerView(addSections(arrayList));
                    VideosEditedListFragment.this.videoRV.setVisibility(0);
                    VideosEditedListFragment.this.message.setVisibility(8);
                }
                VideosEditedListFragment.this.swipeRefreshLayout.setRefreshing(false);
            } catch (Exception unused) {
            }
        }

        private ArrayList<Video> addSections(ArrayList<Video> arrayList) {
            try {
                ArrayList<Video> arrayList2 = new ArrayList<>();
                Date date = new Date();
                Log.d(Const.TAG, "Original Length: " + arrayList.size());
                for (int i = 0; i < arrayList.size(); i++) {
                    Video video = arrayList.get(i);
                    if (i == 0) {
                        arrayList2.add(new Video(true, video.getLastModified()));
                        arrayList2.add(video);
                        date = video.getLastModified();
                    } else {
                        if (addNewSection(date, video.getLastModified())) {
                            arrayList2.add(new Video(true, video.getLastModified()));
                            date = video.getLastModified();
                        }
                        arrayList2.add(video);
                    }
                }
                Log.d(Const.TAG, "Length with sections: " + arrayList2.size());
                return arrayList2;
            } catch (Exception unused) {
                return new ArrayList<>();
            }
        }

        private boolean addNewSection(Date date, Date date2) {
            Calendar calendar = toCalendar(date.getTime());
            int abs = (int) Math.abs((toCalendar(date2.getTime()).getTimeInMillis() - calendar.getTimeInMillis()) / 86400000);
            Log.d(Const.TAG, "Date diff is: " + abs);
            return abs > 0;
        }

        private Calendar toCalendar(long j) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(j);
            instance.set(11, 0);
            instance.set(12, 0);
            instance.set(13, 0);
            instance.set(14, 0);
            return instance;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            Log.d(Const.TAG, "Progress is :" + numArr[0]);
        }

        /* access modifiers changed from: protected */
        public ArrayList<Video> doInBackground(File[]... fileArr) {
            try {
                this.files = fileArr[0];
                for (int i = 0; i < this.files.length; i++) {
                    File file = this.files[i];
                    if (!file.isDirectory() && VideosEditedListFragment.isVideoFile(file.getPath())) {
                        VideosEditedListFragment.this.videosList.add(new Video(file.getName(), file, getBitmap(file), new Date(file.lastModified())));
                        publishProgress(Integer.valueOf(i));
                    }
                }
                return VideosEditedListFragment.this.videosList;
            } catch (Exception unused) {
                return new ArrayList<>();
            }
        }

        /* access modifiers changed from: package-private */
        public Bitmap getBitmap(File file) {
            try {
                Cursor query = this.resolver.query(MediaStore.Video.Media.getContentUri("external"), new String[]{"_id", "bucket_id", "bucket_display_name", "_data"}, "_data=? ", new String[]{file.getPath()}, null);
                if (query != null && query.moveToNext()) {
                    Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(this.resolver, (long) query.getInt(query.getColumnIndexOrThrow("_id")), 1, null);
                    query.close();
                    return thumbnail;
                }
            } catch (Exception unused) {
            }
            return null;
        }
    }
}
