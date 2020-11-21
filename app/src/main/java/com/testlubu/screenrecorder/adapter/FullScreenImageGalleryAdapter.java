package com.testlubu.screenrecorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.viewpager.widget.PagerAdapter;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.common.Utils;
import java.util.List;

public class FullScreenImageGalleryAdapter extends PagerAdapter {
    private FullScreenImageLoader fullScreenImageLoader;
    private final List<String> images;

    public interface FullScreenImageLoader {
        void loadFullScreenImage(ImageView imageView, String str, int i, LinearLayout linearLayout);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    public FullScreenImageGalleryAdapter(List<String> list) {
        this.images = list;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Object instantiateItem(ViewGroup viewGroup, int i) {
        View inflate = ((LayoutInflater) viewGroup.getContext().getSystemService("layout_inflater")).inflate(R.layout.fullscreen_image, (ViewGroup) null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.iv);
        String str = this.images.get(i);
        int screenWidth = Utils.getScreenWidth(imageView.getContext());
        this.fullScreenImageLoader.loadFullScreenImage(imageView, str, screenWidth, (LinearLayout) inflate.findViewById(R.id.ll));
        viewGroup.addView(inflate, 0);
        return inflate;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return this.images.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        viewGroup.removeView((View) obj);
    }

    public void setFullScreenImageLoader(FullScreenImageLoader fullScreenImageLoader2) {
        this.fullScreenImageLoader = fullScreenImageLoader2;
    }
}
