package com.testlubu.screenrecorder.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.testlubu.screenrecorder.R;
import java.util.ArrayList;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private ArrayList<Integer> colors;
    private Context context;
    private int itemCheck = 0;
    private OnClick onClick;

    public interface OnClick {
        void onClickColor(int i);
    }

    public ColorAdapter(Context context2, ArrayList<Integer> arrayList, OnClick onClick2) {
        this.colors = arrayList;
        this.context = context2;
        this.onClick = onClick2;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_color, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Drawable background = viewHolder.imv_color.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(this.colors.get(i).intValue());
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(this.colors.get(i).intValue());
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(this.colors.get(i).intValue());
        }
        if (this.itemCheck == this.colors.get(i).intValue()) {
            viewHolder.imv_check.setVisibility(0);
        } else {
            viewHolder.imv_check.setVisibility(8);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.colors.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imv_check;
        private ImageView imv_color;

        ViewHolder(View view) {
            super(view);
            this.imv_color = (ImageView) view.findViewById(R.id.imv_color);
            this.imv_check = (ImageView) view.findViewById(R.id.imv_check);
            this.itemView.setOnClickListener(new View.OnClickListener() {
                /* class com.testlubu.screenrecorder.adapter.ColorAdapter.ViewHolder.AnonymousClass1 */

                public void onClick(View view) {
                    ColorAdapter.this.itemCheck = ((Integer) ColorAdapter.this.colors.get(ViewHolder.this.getAdapterPosition())).intValue();
                    ColorAdapter.this.onClick.onClickColor(((Integer) ColorAdapter.this.colors.get(ViewHolder.this.getAdapterPosition())).intValue());
                    ColorAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }
}
