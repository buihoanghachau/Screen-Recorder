package com.testlubu.screenrecorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import java.util.ArrayList;

public class AppsListFragmentAdapter extends RecyclerView.Adapter<AppsListFragmentAdapter.SimpleViewHolder> {
    private ArrayList<Apps> apps;
    private OnItemClicked onClick;

    public interface OnItemClicked {
        void onItemClick(int i);
    }

    public AppsListFragmentAdapter(ArrayList<Apps> arrayList) {
        this.apps = arrayList;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public SimpleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SimpleViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_apps_list_preference, viewGroup, false));
    }

    public void onBindViewHolder(final SimpleViewHolder simpleViewHolder, int i) {
        Apps apps2 = this.apps.get(simpleViewHolder.getAdapterPosition());
        TextView textView = simpleViewHolder.textView;
        textView.setText("" + apps2.getAppName());
        simpleViewHolder.appIcon.setImageDrawable(apps2.getAppIcon());
        if (apps2.isSelectedApp()) {
            simpleViewHolder.selectedApp.setVisibility(0);
        } else {
            simpleViewHolder.selectedApp.setVisibility(4);
        }
        simpleViewHolder.app.setOnClickListener(new View.OnClickListener() {
            /* class com.testlubu.screenrecorder.adapter.AppsListFragmentAdapter.AnonymousClass1 */

            public void onClick(View view) {
                try {
                    AppsListFragmentAdapter.this.onClick.onItemClick(simpleViewHolder.getAdapterPosition());
                } catch (Exception unused) {
                }
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.apps.size();
    }

    public void setOnClick(OnItemClicked onItemClicked) {
        this.onClick = onItemClicked;
    }

    /* access modifiers changed from: package-private */
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout app;
        ImageView appIcon;
        ImageView selectedApp;
        TextView textView;

        SimpleViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.appName);
            this.appIcon = (ImageView) view.findViewById(R.id.appIcon);
            this.selectedApp = (ImageView) view.findViewById(R.id.appChecked);
            this.app = (RelativeLayout) view.findViewById(R.id.app);
        }
    }
}
