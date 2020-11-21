package com.testlubu.screenrecorder.adapter.decoration;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int i) {
        this.space = i;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
    public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        rect.bottom = 0;
        rect.top = this.space;
        if (recyclerView.getChildLayoutPosition(view) % 2 == 0) {
            int i = this.space;
            rect.right = i;
            rect.left = i / 2;
            return;
        }
        int i2 = this.space;
        rect.right = i2 / 2;
        rect.left = i2;
    }
}
