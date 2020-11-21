package com.testlubu.screenrecorder.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import com.testlubu.screenrecorder.common.Utils;
import com.testlubu.screenrecorder.model.Photo;
import com.testlubu.screenrecorder.ui.fragments.ScreenshotsListFragment;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_SECTION = 0;
    private Context context;
    private int count = 0;
    private boolean isMultiSelect = false;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        /* class com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter.AnonymousClass1 */

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            PhotoRecyclerAdapter.this.photosListFragment.setEnableSwipe(false);
            actionMode.getMenuInflater().inflate(R.menu.video_list_action_menu, menu);
            return true;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            try {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete) {
                    ArrayList arrayList = new ArrayList();
                    Iterator it = PhotoRecyclerAdapter.this.photos.iterator();
                    while (it.hasNext()) {
                        Photo photo = (Photo) it.next();
                        if (photo.isSelected()) {
                            arrayList.add(photo);
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        PhotoRecyclerAdapter.this.confirmDelete(arrayList);
                    }
                    PhotoRecyclerAdapter.this.mActionMode.finish();
                } else if (itemId == R.id.select_all) {
                    Iterator it2 = PhotoRecyclerAdapter.this.photos.iterator();
                    while (it2.hasNext()) {
                        ((Photo) it2.next()).setSelected(true);
                    }
                    ActionMode actionMode2 = PhotoRecyclerAdapter.this.mActionMode;
                    actionMode2.setTitle("" + PhotoRecyclerAdapter.this.photos.size());
                    PhotoRecyclerAdapter.this.notifyDataSetChanged();
                } else if (itemId == R.id.share) {
                    ArrayList arrayList2 = new ArrayList();
                    Iterator it3 = PhotoRecyclerAdapter.this.photos.iterator();
                    while (it3.hasNext()) {
                        Photo photo2 = (Photo) it3.next();
                        if (photo2.isSelected()) {
                            arrayList2.add(Integer.valueOf(PhotoRecyclerAdapter.this.photos.indexOf(photo2)));
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        PhotoRecyclerAdapter.this.sharePhotos(arrayList2);
                    }
                    PhotoRecyclerAdapter.this.mActionMode.finish();
                }
            } catch (Exception unused) {
            }
            return true;
        }

        @Override // androidx.appcompat.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode actionMode) {
            try {
                Iterator it = PhotoRecyclerAdapter.this.photos.iterator();
                while (it.hasNext()) {
                    ((Photo) it.next()).setSelected(false);
                }
                PhotoRecyclerAdapter.this.isMultiSelect = false;
                PhotoRecyclerAdapter.this.photosListFragment.setEnableSwipe(true);
                PhotoRecyclerAdapter.this.notifyDataSetChanged();
            } catch (Exception unused) {
            }
        }
    };
    private ArrayList<Photo> photos;
    private ScreenshotsListFragment photosListFragment;

    static /* synthetic */ int access$808(PhotoRecyclerAdapter photoRecyclerAdapter) {
        int i = photoRecyclerAdapter.count;
        photoRecyclerAdapter.count = i + 1;
        return i;
    }

    static /* synthetic */ int access$810(PhotoRecyclerAdapter photoRecyclerAdapter) {
        int i = photoRecyclerAdapter.count;
        photoRecyclerAdapter.count = i - 1;
        return i;
    }

    public PhotoRecyclerAdapter(Context context2, ArrayList<Photo> arrayList, ScreenshotsListFragment screenshotsListFragment) {
        this.photos = arrayList;
        this.context = context2;
        this.photosListFragment = screenshotsListFragment;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return isSection(i)?0:1;
    }

    public boolean isSection(int i) {
        return this.photos.get(i).isSection();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 0) {
            return new SectionViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_section, viewGroup, false));
        }
        if (i != 1) {
            return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_photo, viewGroup, false));
        }
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_photo, viewGroup, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final Photo photo = this.photos.get(i);
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType == 0) {
            ((SectionViewHolder) viewHolder).section.setText(Utils.generateSectionTitle(photo.getLastModified()));
        } else if (itemViewType == 1) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            if (this.photos.get(i).getThumbnail() != null) {
                itemViewHolder.iv_thumbnail.setImageBitmap(photo.getThumbnail());
            } else {
                itemViewHolder.iv_thumbnail.setImageResource(0);
            }
            if (photo.isSelected()) {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, R.color.multiSelectColor)));
            } else {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, 17170445)));
            }
            itemViewHolder.photoCard.setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter.AnonymousClass2 */

                public void onClick(View view) {
                    if (PhotoRecyclerAdapter.this.isMultiSelect) {
                        if (photo.isSelected()) {
                            PhotoRecyclerAdapter.access$810(PhotoRecyclerAdapter.this);
                        } else {
                            PhotoRecyclerAdapter.access$808(PhotoRecyclerAdapter.this);
                        }
                        photo.setSelected(true ^ photo.isSelected());
                        PhotoRecyclerAdapter.this.notifyDataSetChanged();
                        ActionMode actionMode = PhotoRecyclerAdapter.this.mActionMode;
                        actionMode.setTitle("" + PhotoRecyclerAdapter.this.count);
                        if (PhotoRecyclerAdapter.this.count == 0) {
                            PhotoRecyclerAdapter.this.setMultiSelect(false);
                            return;
                        }
                        return;
                    }
                    try {
                        File file = photo.getFile();
                        Context context = PhotoRecyclerAdapter.this.context;
                        Uri uriForFile = FileProvider.getUriForFile(context, PhotoRecyclerAdapter.this.context.getPackageName() + ".provider", file);
                        Log.d(Const.TAG, uriForFile.toString());
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW").addFlags(1).setDataAndType(uriForFile, PhotoRecyclerAdapter.this.context.getContentResolver().getType(uriForFile));
                        PhotoRecyclerAdapter.this.context.startActivity(intent);
                    } catch (Exception unused) {
                    }
                }
            });
            itemViewHolder.photoCard.setOnLongClickListener(new View.OnLongClickListener() {
                /* class com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter.AnonymousClass3 */

                public boolean onLongClick(View view) {
                    if (!PhotoRecyclerAdapter.this.isMultiSelect) {
                        PhotoRecyclerAdapter.this.setMultiSelect(true);
                        photo.setSelected(true);
                        PhotoRecyclerAdapter.access$808(PhotoRecyclerAdapter.this);
                        ActionMode actionMode = PhotoRecyclerAdapter.this.mActionMode;
                        actionMode.setTitle("" + PhotoRecyclerAdapter.this.count);
                        PhotoRecyclerAdapter.this.notifyDataSetChanged();
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
            this.mActionMode = ((AppCompatActivity) this.photosListFragment.getActivity()).startSupportActionMode(this.mActionModeCallback);
            return;
        }
        this.isMultiSelect = false;
        this.mActionMode.finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sharePhotos(ArrayList<Integer> arrayList) {
        try {
            ArrayList<Uri> arrayList2 = new ArrayList<>();
            Iterator<Integer> it = arrayList.iterator();
            while (it.hasNext()) {
                int intValue = it.next().intValue();
                Context context2 = this.context;
                arrayList2.add(FileProvider.getUriForFile(context2, this.context.getPackageName() + ".provider", this.photos.get(intValue).getFile()));
            }
            this.context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND_MULTIPLE").setType("photo/*").setFlags(1).putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList2), this.context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deletePhotos(ArrayList<Photo> arrayList) {
        try {
            Iterator<Photo> it = arrayList.iterator();
            while (it.hasNext()) {
                Photo next = it.next();
                if (!next.isSection() && next.getFile().delete()) {
                    notifyItemRemoved(this.photos.indexOf(next));
                    this.photos.remove(next);
                }
            }
            notifyDataSetChanged();
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void confirmDelete(final ArrayList<Photo> arrayList) {
        try {
            int size = arrayList.size();
            new AlertDialog.Builder(this.context).setTitle(this.context.getResources().getQuantityString(R.plurals.delete_photo_alert_title, size)).setMessage(this.context.getResources().getQuantityString(R.plurals.delete_photo_alert_message, size, Integer.valueOf(size))).setCancelable(false).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter.AnonymousClass5 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    PhotoRecyclerAdapter.this.deletePhotos(arrayList);
                }
            }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.PhotoRecyclerAdapter.AnonymousClass4 */

                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception unused) {
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.photos.size();
    }

    private final class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_thumbnail;
        private RelativeLayout photoCard;
        private FrameLayout selectableFrame;

        ItemViewHolder(View view) {
            super(view);
            this.iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.iv_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            this.photoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            this.selectableFrame = (FrameLayout) view.findViewById(R.id.selectableFrame);
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
