package com.testlubu.screenrecorder.folderpicker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Const;
import java.io.File;
import java.util.ArrayList;

public class DirectoryRecyclerAdapter extends RecyclerView.Adapter<DirectoryRecyclerAdapter.ItemViewHolder> {
    private static OnDirectoryClickedListerner onDirectoryClickedListerner;
    private Context context;
    private ArrayList<File> directories;

    public interface OnDirectoryClickedListerner {
        void OnDirectoryClicked(File file);
    }

    DirectoryRecyclerAdapter(Context context2, OnDirectoryClickedListerner onDirectoryClickedListerner2, ArrayList<File> arrayList) {
        this.context = context2;
        onDirectoryClickedListerner = onDirectoryClickedListerner2;
        this.directories = arrayList;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_directory_chooser, viewGroup, false));
    }

    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.dir.setText(this.directories.get(i).getName());
        itemViewHolder.dir.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.folderpicker.DirectoryRecyclerAdapter.AnonymousClass1 */

            public void onClick(View view) {
                Log.d(Const.TAG, "Item clicked: " + DirectoryRecyclerAdapter.this.directories.get(itemViewHolder.getAdapterPosition()));
                DirectoryRecyclerAdapter.onDirectoryClickedListerner.OnDirectoryClicked((File) DirectoryRecyclerAdapter.this.directories.get(itemViewHolder.getAdapterPosition()));
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.directories.size();
    }

    /* access modifiers changed from: package-private */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView dir;
        LinearLayout dir_view;

        public ItemViewHolder(View view) {
            super(view);
            this.dir = (TextView) view.findViewById(R.id.directory);
            this.dir_view = (LinearLayout) view.findViewById(R.id.directory_view);
        }
    }
}
