package com.testlubu.screenrecorder.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.encoder.Mp4toGIFConverter;
import com.testlubu.screenrecorder.model.Video;
import com.testlubu.screenrecorder.ui.activities.EditVideoActivity;
import com.testlubu.screenrecorder.ui.fragments.VideosListFragment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_SECTION = 0;
    private Context context;
    private int count = 0;
    private boolean isMultiSelect = false;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass1 */

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            VideoRecyclerAdapter.this.videosListFragment.setEnableSwipe(false);
            actionMode.getMenuInflater().inflate(R.menu.video_list_action_menu, menu);
            return true;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            try {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete) {
                    ArrayList arrayList = new ArrayList();
                    Iterator it = VideoRecyclerAdapter.this.videos.iterator();
                    while (it.hasNext()) {
                        Video video = (Video) it.next();
                        if (video.isSelected()) {
                            arrayList.add(video);
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        VideoRecyclerAdapter.this.confirmDelete(arrayList);
                    }
                    VideoRecyclerAdapter.this.mActionMode.finish();
                } else if (itemId == R.id.select_all) {
                    Iterator it2 = VideoRecyclerAdapter.this.videos.iterator();
                    while (it2.hasNext()) {
                        ((Video) it2.next()).setSelected(true);
                    }
                    ActionMode actionMode2 = VideoRecyclerAdapter.this.mActionMode;
                    actionMode2.setTitle("" + VideoRecyclerAdapter.this.videos.size());
                    VideoRecyclerAdapter.this.notifyDataSetChanged();
                } else if (itemId == R.id.share) {
                    ArrayList arrayList2 = new ArrayList();
                    Iterator it3 = VideoRecyclerAdapter.this.videos.iterator();
                    while (it3.hasNext()) {
                        Video video2 = (Video) it3.next();
                        if (video2.isSelected()) {
                            arrayList2.add(Integer.valueOf(VideoRecyclerAdapter.this.videos.indexOf(video2)));
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        VideoRecyclerAdapter.this.shareVideos(arrayList2);
                    }
                    VideoRecyclerAdapter.this.mActionMode.finish();
                }
            } catch (Exception unused) {
            }
            return true;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode actionMode) {
            Iterator it = VideoRecyclerAdapter.this.videos.iterator();
            while (it.hasNext()) {
                ((Video) it.next()).setSelected(false);
            }
            VideoRecyclerAdapter.this.isMultiSelect = false;
            VideoRecyclerAdapter.this.videosListFragment.setEnableSwipe(true);
            VideoRecyclerAdapter.this.notifyDataSetChanged();
        }
    };
    private ArrayList<Video> videos;
    private VideosListFragment videosListFragment;

    static /* synthetic */ int access$1408(VideoRecyclerAdapter videoRecyclerAdapter) {
        int i = videoRecyclerAdapter.count;
        videoRecyclerAdapter.count = i + 1;
        return i;
    }

    static /* synthetic */ int access$1410(VideoRecyclerAdapter videoRecyclerAdapter) {
        int i = videoRecyclerAdapter.count;
        videoRecyclerAdapter.count = i - 1;
        return i;
    }

    public VideoRecyclerAdapter(Context context2, ArrayList<Video> arrayList, VideosListFragment videosListFragment2) {
        this.videos = arrayList;
        this.context = context2;
        this.videosListFragment = videosListFragment2;
    }

    public ArrayList<Video> getVideos() {
        return this.videos;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return isSection(i)?0:1;
    }

    public boolean isSection(int i) {
        return this.videos.get(i).isSection();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i != 0) {
            return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video, viewGroup, false));
        }
        return new SectionViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_section, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        final Video video = this.videos.get(i);
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType == 0) {
            ((SectionViewHolder) viewHolder).section.setText(generateSectionTitle(video.getLastModified()));
        } else if (itemViewType == 1) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.tv_fileName.setText(video.getFileName());
            if (this.videos.get(i).getThumbnail() != null) {
                itemViewHolder.iv_thumbnail.setImageBitmap(video.getThumbnail());
            } else {
                itemViewHolder.iv_thumbnail.setImageResource(0);
                Log.d(Const.TAG, "thumbnail error");
            }
            if (this.isMultiSelect) {
                itemViewHolder.iv_play.setVisibility(4);
                itemViewHolder.overflow.setVisibility(4);
            } else {
                itemViewHolder.iv_play.setVisibility(0);
                itemViewHolder.overflow.setVisibility(0);
            }
            if (video.isSelected()) {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, R.color.multiSelectColor)));
            } else {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, 17170445)));
            }
            itemViewHolder.overflow.setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass2 */

                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(VideoRecyclerAdapter.this.context, view);
                    popupMenu.inflate(R.menu.popupmenu);
                    popupMenu.show();
                    popupMenu.getMenu().getItem(3).setEnabled(PreferenceManager.getDefaultSharedPreferences(VideoRecyclerAdapter.this.context).getBoolean(VideoRecyclerAdapter.this.context.getString(R.string.preference_save_gif_key), true));
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.delete /*{ENCODED_INT: 2131361912}*/:
                                    VideoRecyclerAdapter.this.confirmDelete(viewHolder.getAdapterPosition());
                                    return true;
                                case R.id.edit /*{ENCODED_INT: 2131361927}*/:
                                    Intent intent = new Intent(VideoRecyclerAdapter.this.context, EditVideoActivity.class);
                                    Log.d(Const.TAG, "Uri: " + Uri.fromFile(video.getFile()));
                                    intent.putExtra(Const.VIDEO_EDIT_URI_KEY, Uri.fromFile(video.getFile()).toString());
                                    VideoRecyclerAdapter.this.videosListFragment.startActivityForResult(intent, Const.VIDEO_EDIT_REQUEST_CODE);
                                    return true;
                                case R.id.savegif /*{ENCODED_INT: 2131362116}*/:
                                    Mp4toGIFConverter mp4toGIFConverter = new Mp4toGIFConverter(VideoRecyclerAdapter.this.context);
                                    mp4toGIFConverter.setVideoUri(Uri.fromFile(video.getFile()));
                                    mp4toGIFConverter.convertToGif();
                                    return true;
                                case R.id.share /*{ENCODED_INT: 2131362142}*/:
                                    VideoRecyclerAdapter.this.shareVideo(itemViewHolder.getAdapterPosition());
                                    return true;
                                default:
                                    return true;
                            }
                        }
                    });
                }
            });
            itemViewHolder.videoCard.setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass3 */

                public void onClick(View view) {
                    if (VideoRecyclerAdapter.this.isMultiSelect) {
                        if (video.isSelected()) {
                            VideoRecyclerAdapter.access$1410(VideoRecyclerAdapter.this);
                        } else {
                            VideoRecyclerAdapter.access$1408(VideoRecyclerAdapter.this);
                        }
                        video.setSelected(true ^ video.isSelected());
                        VideoRecyclerAdapter.this.notifyDataSetChanged();
                        ActionMode actionMode = VideoRecyclerAdapter.this.mActionMode;
                        actionMode.setTitle("" + VideoRecyclerAdapter.this.count);
                        if (VideoRecyclerAdapter.this.count == 0) {
                            VideoRecyclerAdapter.this.setMultiSelect(false);
                            return;
                        }
                        return;
                    }
                    try {
                        File file = video.getFile();
                        Log.d("Videos List", "video position clicked: " + itemViewHolder.getAdapterPosition());
                        Context context = VideoRecyclerAdapter.this.context;
                        Uri uriForFile = FileProvider.getUriForFile(context, VideoRecyclerAdapter.this.context.getPackageName() + ".provider", file);
                        Log.d(Const.TAG, uriForFile.toString());
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW").addFlags(1).setDataAndType(uriForFile, VideoRecyclerAdapter.this.context.getContentResolver().getType(uriForFile));
                        VideoRecyclerAdapter.this.context.startActivity(intent);
                    } catch (Exception unused) {
                    }
                }
            });
            itemViewHolder.videoCard.setOnLongClickListener(new View.OnLongClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass4 */

                public boolean onLongClick(View view) {
                    if (!VideoRecyclerAdapter.this.isMultiSelect) {
                        VideoRecyclerAdapter.this.setMultiSelect(true);
                        video.setSelected(true);
                        VideoRecyclerAdapter.access$1408(VideoRecyclerAdapter.this);
                        ActionMode actionMode = VideoRecyclerAdapter.this.mActionMode;
                        actionMode.setTitle("" + VideoRecyclerAdapter.this.count);
                        VideoRecyclerAdapter.this.notifyDataSetChanged();
                    }
                    return true;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMultiSelect(boolean z) {
        if (z) {
            this.isMultiSelect = true;
            this.count = 0;
            this.mActionMode = ((AppCompatActivity) this.videosListFragment.getActivity()).startSupportActionMode(this.mActionModeCallback);
            return;
        }
        this.isMultiSelect = false;
        this.mActionMode.finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void shareVideo(int i) {
        try {
            Context context2 = this.context;
            this.context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND").setType("video/*").setFlags(1).putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(context2, this.context.getPackageName() + ".provider", this.videos.get(i).getFile())), this.context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void shareVideos(ArrayList<Integer> arrayList) {
        try {
            ArrayList<Uri> arrayList2 = new ArrayList<>();
            Iterator<Integer> it = arrayList.iterator();
            while (it.hasNext()) {
                int intValue = it.next().intValue();
                Context context2 = this.context;
                arrayList2.add(FileProvider.getUriForFile(context2, this.context.getPackageName() + ".provider", this.videos.get(intValue).getFile()));
            }
            this.context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND_MULTIPLE").setType("video/*").setFlags(1).putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList2), this.context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteVideo(int i) {
        try {
            Log.d("Videos List", "delete position clicked: " + i);
            File file = new File(this.videos.get(i).getFile().getPath());
            ContentResolver contentResolver = this.context.getContentResolver();
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            contentResolver.delete(uri, "_id =" + this.videos.get(i).getIdVideo(), null);
            file.delete();
            Toast.makeText(this.context, this.context.getString(R.string.file_delete_successfuly), 0).show();
            this.videos.remove(i);
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, this.videos.size());
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteVideos(ArrayList<Video> arrayList) {
        try {
            Iterator<Video> it = arrayList.iterator();
            while (it.hasNext()) {
                Video next = it.next();
                if (!next.isSection() && next.getFile().delete()) {
                    notifyItemRemoved(this.videos.indexOf(next));
                    this.videos.remove(next);
                }
            }
            notifyDataSetChanged();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void confirmDelete(final int i) {
        try {
            new AlertDialog.Builder(this.context).setTitle(this.context.getResources().getQuantityString(R.plurals.delete_alert_title, 1)).setMessage(this.context.getResources().getQuantityString(R.plurals.delete_alert_message, 1)).setCancelable(false).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass6 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    VideoRecyclerAdapter.this.deleteVideo(i);
                }
            }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass5 */

                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void confirmDelete(final ArrayList<Video> arrayList) {
        try {
            int size = arrayList.size();
            new AlertDialog.Builder(this.context).setTitle(this.context.getResources().getQuantityString(R.plurals.delete_alert_title, size)).setMessage(this.context.getResources().getQuantityString(R.plurals.delete_alert_message, size, Integer.valueOf(size))).setCancelable(false).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass8 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    VideoRecyclerAdapter.this.deleteVideos(arrayList);
                }
            }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.VideoRecyclerAdapter.AnonymousClass7 */

                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception unused) {
        }
    }

    private String generateSectionTitle(Date date) {
        Calendar calendar = Utils.toCalendar(new Date().getTime());
        Calendar calendar2 = Utils.toCalendar(date.getTime());
        int abs = (int) Math.abs((calendar2.getTimeInMillis() - calendar.getTimeInMillis()) / 86400000);
        if (calendar.get(1) - calendar2.get(1) != 0) {
            return new SimpleDateFormat("EEEE, dd MMM YYYY", Locale.getDefault()).format(date);
        }
        if (abs != 0) {
            return abs != 1 ? new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(date) : "Yesterday";
        }
        return "Today";
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.videos.size();
    }

    private final class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_play;
        private ImageView iv_thumbnail;
        private ImageButton overflow;
        private FrameLayout selectableFrame;
        private TextView tv_fileName;
        private RelativeLayout videoCard;

        ItemViewHolder(View view) {
            super(view);
            this.tv_fileName = (TextView) view.findViewById(R.id.fileName);
            this.iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.iv_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            this.videoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            this.overflow = (ImageButton) view.findViewById(R.id.ic_overflow);
            this.selectableFrame = (FrameLayout) view.findViewById(R.id.selectableFrame);
            this.iv_play = (ImageView) view.findViewById(R.id.play_iv);
        }
    }

    private final class SectionViewHolder extends RecyclerView.ViewHolder {
        private TextView section;

        SectionViewHolder(View view) {
            super(view);
            this.section = (TextView) view.findViewById(R.id.sectionID);
        }
    }
}
