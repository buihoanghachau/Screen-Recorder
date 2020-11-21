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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter;
import com.testlubu.screenrecorder.adapter.decoration.SpacesItemDecoration;
import com.testlubu.screenrecorder.common.Cache;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.interfaces.PermissionResultListener;
import com.testlubu.screenrecorder.listener.ObserverInterface;
import com.testlubu.screenrecorder.listener.ObserverUtils;
import com.testlubu.screenrecorder.model.Video;
import com.testlubu.screenrecorder.model.listener.EvbRecordTime;
import com.testlubu.screenrecorder.model.listener.EvbStageRecord;
import com.testlubu.screenrecorder.model.listener.EvbStopService;
import com.testlubu.screenrecorder.services.FloatingControlService;
import com.testlubu.screenrecorder.services.RecorderService;
import com.testlubu.screenrecorder.ui.activities.HomeActivity;
import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class VideosListFragment extends BaseFragment implements PermissionResultListener, SwipeRefreshLayout.OnRefreshListener, ObserverInterface {
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    private View btnFloatButton;
    private ImageView imRecord;
    private View loRecord;
    private VideoRecyclerAdapter mAdapter;
    BroadcastReceiver mReceiverUpdate = new BroadcastReceiver() {
        /* class com.testlubu.screenrecorder.ui.fragments.VideosListFragment.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (Const.UPDATE_UI.equals(intent.getAction())) {
                VideosListFragment.this.onRefresh();
            }
        }
    };
    private TextView message;
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTimeRecord;
    private RecyclerView videoRV;
    private ArrayList<Video> videosList = new ArrayList<>();

    public static VideosListFragment newInstance() {
        return new VideosListFragment();
    }

    /* access modifiers changed from: private */
    public static boolean isVideoFile(String str) {
        String guessContentTypeFromName = URLConnection.guessContentTypeFromName(str);
        return guessContentTypeFromName != null && guessContentTypeFromName.startsWith("video");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_videos, viewGroup, false);
        ObserverUtils.getInstance().registerObserver((ObserverInterface) this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.UPDATE_UI);
        getActivity().registerReceiver(this.mReceiverUpdate, intentFilter);
        this.message = (TextView) inflate.findViewById(R.id.message_tv);
        this.videoRV = (RecyclerView) inflate.findViewById(R.id.videos_rv);
        this.btnFloatButton = inflate.findViewById(R.id.btn_floatbutton);
        this.loRecord = inflate.findViewById(R.id.lo_record);
        this.tvTimeRecord = (TextView) inflate.findViewById(R.id.tv_time_record);
        this.imRecord = (ImageView) inflate.findViewById(R.id.im_record);
        this.videoRV.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(10.0f, getActivity())));
        this.swipeRefreshLayout = (SwipeRefreshLayout) inflate.findViewById(R.id.swipeRefresh);
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, 17170453, 17170457, 17170451);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initControl();
        return inflate;
    }

    private void initControl() {
        this.btnFloatButton.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.ui.fragments.VideosListFragment.AnonymousClass1 */

            public void onClick(View view) {
                if (!FloatingControlService.isCountdown) {
                    VideosListFragment.this.showFloatbtnRecord(RecorderService.isRecording);
                    ObserverUtils.getInstance().notifyObservers(new EvbStageRecord(!FloatingControlService.isRecording));
                }
            }
        });
    }

    public void showFloatbtnRecord(boolean z) {
        if (!z) {
            this.loRecord.setVisibility(4);
            this.imRecord.setVisibility(0);
            return;
        }
        this.loRecord.setVisibility(0);
        this.imRecord.setVisibility(4);
    }

    public ArrayList<Video> getVideosList() {
        return this.videosList;
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

    @RequiresApi(api = 23)
    public void onResume() {
        if (hasPermissions(getContext(), this.PERMISSIONS) && FloatingControlService.getInstance() == null) {
            ((HomeActivity) getActivity()).startService();
            this.btnFloatButton.setVisibility(0);
        }
        super.onResume();
    }

    public void onStart() {
        super.onStart();
    }

    @Override // android.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override // com.testlubu.screenrecorder.ui.fragments.BaseFragment
    public void onVisibleFragment() {
        super.onVisibleFragment();
        setRecyclerView(Cache.getInstance().getArrVideos());
        if (getActivity() != null) {
            this.videosList.clear();
            checkPermission();
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onDetach() {
        super.onDetach();
        if (this.mAdapter != null) {
            Cache.getInstance().setArrVideos(this.mAdapter.getVideos());
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mReceiverUpdate);
        super.onDestroy();
    }

    public void onStop() {
        super.onStop();
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
            /* class com.testlubu.screenrecorder.ui.fragments.VideosListFragment.AnonymousClass2 */

            public boolean onMenuItemClick(MenuItem menuItem) {
                if (VideosListFragment.this.swipeRefreshLayout.isRefreshing()) {
                    return false;
                }
                VideosListFragment.this.videosList.clear();
                VideosListFragment.this.checkPermission();
                Log.d(Const.TAG, "Refreshing");
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPermission() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).startService();
                    this.btnFloatButton.setVisibility(0);
                }
                if (this.videosList.isEmpty()) {
                    SharedPreferences sharedPreferences = this.prefs;
                    String string = getString(R.string.savelocation_key);
                    File file = new File(sharedPreferences.getString(string, Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR));
                    if (!file.exists()) {
                        Utils.createDir();
                        Log.d(Const.TAG, "Directory missing! Creating dir");
                    }
                    ArrayList arrayList = new ArrayList();
                    if (file.isDirectory() && file.exists()) {
                        arrayList.addAll(Arrays.asList(getVideos(file.listFiles())));
                    }
                    new GetVideosAsync().execute((File[]) arrayList.toArray(new File[arrayList.size()]));
                }
            } else if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).setPermissionResultListener(this);
                ((HomeActivity) getActivity()).requestPermissionStorage();
                boolean z = getActivity() instanceof HomeActivity;
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
            this.mAdapter = new VideoRecyclerAdapter(getActivity(), arrayList, this);
            this.videoRV.setAdapter(this.mAdapter);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                /* class com.testlubu.screenrecorder.ui.fragments.VideosListFragment.AnonymousClass3 */

                @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
                public int getSpanSize(int i) {
                    try {
                        if (VideosListFragment.this.mAdapter.isSection(i)) {
                            return gridLayoutManager.getSpanCount();
                        }
                        return 1;
                    } catch (Exception unused) {
                        return 1;
                    }
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

    @Override // com.testlubu.screenrecorder.listener.ObserverInterface
    public void notifyAction(Object obj) {
        if (obj instanceof EvbRecordTime) {
            showFloatbtnRecord(true);
            this.tvTimeRecord.setText(((EvbRecordTime) obj).time);
        } else if (obj instanceof EvbStageRecord) {
            this.tvTimeRecord.setText("00:00");
            showFloatbtnRecord(((EvbStageRecord) obj).isStart);
        } else if (obj instanceof EvbStopService) {
            this.btnFloatButton.setVisibility(8);
        }
    }

    /* access modifiers changed from: package-private */
    public class GetVideosAsync extends AsyncTask<File[], Integer, ArrayList<Video>> {
        File[] files;
        ContentResolver resolver;

        GetVideosAsync() {
            this.resolver = VideosListFragment.this.getActivity().getApplicationContext().getContentResolver();
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            VideosListFragment.this.swipeRefreshLayout.setRefreshing(true);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ArrayList<Video> arrayList) {
            try {
                if (arrayList.isEmpty()) {
                    VideosListFragment.this.videoRV.setVisibility(8);
                    VideosListFragment.this.message.setVisibility(0);
                } else {
                    Collections.sort(arrayList, Collections.reverseOrder());
                    VideosListFragment.this.setRecyclerView(addSections(arrayList));
                    VideosListFragment.this.videoRV.setVisibility(0);
                    VideosListFragment.this.message.setVisibility(8);
                }
                VideosListFragment.this.swipeRefreshLayout.setRefreshing(false);
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
                    long j = getidVideo(file);
                    Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(this.resolver, j, 1, null);
                    if (!file.isDirectory() && VideosListFragment.isVideoFile(file.getPath()) && !file.getName().endsWith("_.mp4")) {
                        VideosListFragment.this.videosList.add(new Video(j, file.getName(), file, thumbnail, new Date(file.lastModified())));
                        publishProgress(Integer.valueOf(i));
                    }
                }
                return VideosListFragment.this.videosList;
            } catch (Exception unused) {
                return new ArrayList<>();
            }
        }

        private long getidVideo(File file) {
            Cursor query = this.resolver.query(MediaStore.Video.Media.getContentUri("external"), new String[]{"_id", "bucket_id", "bucket_display_name", "_data"}, "_data=? ", new String[]{file.getPath()}, null);
            if (query == null || !query.moveToNext()) {
                return -1;
            }
            int i = query.getInt(query.getColumnIndexOrThrow("_id"));
            query.close();
            return (long) i;
        }
    }
}
