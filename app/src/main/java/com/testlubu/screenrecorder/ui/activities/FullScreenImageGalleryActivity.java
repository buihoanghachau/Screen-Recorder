package com.testlubu.screenrecorder.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.testlubu.screenrecorder.R;
import com.testlubu.screenrecorder.adapter.FullScreenImageGalleryAdapter;
import java.util.ArrayList;
import java.util.List;

public class FullScreenImageGalleryActivity extends AppCompatActivity implements FullScreenImageGalleryAdapter.FullScreenImageLoader {
    public static final String KEY_IMAGES = "KEY_IMAGES";
    public static final String KEY_POSITION = "KEY_POSITION";
    private static FullScreenImageGalleryAdapter.FullScreenImageLoader fullScreenImageLoader;
    private List<String> images;
    private int position;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private final ViewPager.OnPageChangeListener viewPagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        /* class com.testlubu.screenrecorder.ui.activities.FullScreenImageGalleryActivity.AnonymousClass1 */

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int i) {
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            if (FullScreenImageGalleryActivity.this.viewPager != null) {
                FullScreenImageGalleryActivity.this.viewPager.setCurrentItem(i);
                FullScreenImageGalleryActivity.this.setActionBarTitle(i);
            }
        }
    };

    /* access modifiers changed from: protected */
    @Override // androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        Bundle extras;
        super.onCreate(bundle);
        setContentView(R.layout.activity_full_screen_image_gallery);
        bindViews();
        setSupportActionBar(this.toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (!(intent == null || (extras = intent.getExtras()) == null)) {
            this.images = extras.getStringArrayList(KEY_IMAGES);
            this.position = extras.getInt(KEY_POSITION);
        }
        setUpViewPager();
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        onBackPressed();
        return true;
    }

    @Override // com.testlubu.screenrecorder.adapter.FullScreenImageGalleryAdapter.FullScreenImageLoader
    public void loadFullScreenImage(ImageView imageView, String str, int i, LinearLayout linearLayout) {
        fullScreenImageLoader.loadFullScreenImage(imageView, str, i, linearLayout);
    }

    private void bindViews() {
        this.viewPager = (ViewPager) findViewById(R.id.vp);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpViewPager() {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.images);
        FullScreenImageGalleryAdapter fullScreenImageGalleryAdapter = new FullScreenImageGalleryAdapter(arrayList);
        fullScreenImageGalleryAdapter.setFullScreenImageLoader(this);
        this.viewPager.setAdapter(fullScreenImageGalleryAdapter);
        this.viewPager.addOnPageChangeListener(this.viewPagerOnPageChangeListener);
        this.viewPager.setCurrentItem(this.position);
        setActionBarTitle(this.position);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActionBarTitle(int i) {
        if (this.viewPager != null && this.images.size() > 1) {
            int count = this.viewPager.getAdapter().getCount();
            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setTitle(String.format("%d/%d", Integer.valueOf(i + 1), Integer.valueOf(count)));
            }
        }
    }

    private void removeListeners() {
        this.viewPager.removeOnPageChangeListener(this.viewPagerOnPageChangeListener);
    }

    public static void setFullScreenImageLoader(FullScreenImageGalleryAdapter.FullScreenImageLoader fullScreenImageLoader2) {
        fullScreenImageLoader = fullScreenImageLoader2;
    }
}
